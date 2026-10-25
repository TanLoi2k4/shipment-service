package com.tlcn.shipment_service.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.tlcn.shipment_service.dto.OrderPageResponse;
import com.tlcn.shipment_service.dto.OrderResponse;
import com.tlcn.shipment_service.dto.OrderSummaryResponse;
import com.tlcn.shipment_service.dto.ResponseDTO;
import com.tlcn.shipment_service.dto.SubOrderResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderServiceClient {

    private static final int PAGE_SIZE = 50;
    private static final int MAX_PAGES = 20;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${order-service.base-url:http://localhost:8084}")
    private String orderServiceBaseUrl;

    public Set<Long> getCustomerSubOrderIds(String authorizationHeader) {
        Set<Long> subOrderIds = new HashSet<>();
        int page = 0;

        while (page < MAX_PAGES) {
            OrderPageResponse<OrderSummaryResponse> pageData = fetchOrderSummaries(authorizationHeader, page);
            if (pageData == null || pageData.getItems() == null || pageData.getItems().isEmpty()) {
                break;
            }

            for (OrderSummaryResponse summary : pageData.getItems()) {
                if (summary.getOrderCode() == null || summary.getOrderCode().isBlank()) {
                    continue;
                }
                OrderResponse detail = fetchOrderDetail(authorizationHeader, summary.getOrderCode());
                if (detail == null || detail.getSubOrders() == null) {
                    continue;
                }
                for (SubOrderResponse subOrder : detail.getSubOrders()) {
                    if (subOrder.getId() != null) {
                        subOrderIds.add(subOrder.getId());
                    }
                }
            }

            if (pageData.isLast()) {
                break;
            }
            page++;
        }

        return subOrderIds;
    }

    private OrderPageResponse<OrderSummaryResponse> fetchOrderSummaries(String authorizationHeader, int page) {
        String url = UriComponentsBuilder
            .fromHttpUrl(orderServiceBaseUrl + "/orders")
            .queryParam("page", page)
            .queryParam("size", PAGE_SIZE)
            .toUriString();

        try {
            ResponseEntity<ResponseDTO<OrderPageResponse<OrderSummaryResponse>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(authorizationHeader)),
                new ParameterizedTypeReference<ResponseDTO<OrderPageResponse<OrderSummaryResponse>>>() {}
            );
            ResponseDTO<OrderPageResponse<OrderSummaryResponse>> body = response.getBody();
            if (body == null || !body.isSuccess()) {
                return null;
            }
            return body.getData();
        } catch (Exception ex) {
            log.warn("Failed to fetch order summaries from order-service page={}: {}", page, ex.getMessage());
            return null;
        }
    }

    private OrderResponse fetchOrderDetail(String authorizationHeader, String orderCode) {
        String url = orderServiceBaseUrl + "/orders/" + orderCode;
        try {
            ResponseEntity<ResponseDTO<OrderResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(authorizationHeader)),
                new ParameterizedTypeReference<ResponseDTO<OrderResponse>>() {}
            );
            ResponseDTO<OrderResponse> body = response.getBody();
            if (body == null || !body.isSuccess()) {
                return null;
            }
            return body.getData();
        } catch (Exception ex) {
            log.warn("Failed to fetch order detail orderCode={}: {}", orderCode, ex.getMessage());
            return null;
        }
    }

    private HttpHeaders authHeaders(String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        return headers;
    }
}
