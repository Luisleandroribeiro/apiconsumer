package com.hidroweb.apiconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients  // 🔹 Habilita o OpenFeign para injetar o cliente automaticamente
public class ApiconsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiconsumerApplication.class, args);
	}
}
