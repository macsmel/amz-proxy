package com.amz.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ProxyApplication {
	public static void main(String[] args) {
		log.info("Starting");
		SpringApplication.run(ProxyApplication.class, args);
	}

}
