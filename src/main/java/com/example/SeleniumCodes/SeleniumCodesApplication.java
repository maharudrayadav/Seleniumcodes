package com.example.SeleniumCodes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.SeleniumCodes", "controller"})
public class SeleniumCodesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeleniumCodesApplication.class, args);
	}

}
