package com.tlcn.shipment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ShipmentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShipmentServiceApplication.class, args);
	}

}
