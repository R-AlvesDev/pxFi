package com.pxfi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PxFiApplication {
	public static void main(String[] args) {
		SpringApplication.run(PxFiApplication.class, args);
	}
}
