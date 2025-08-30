package com.example.SeleniumCodes;

import org.springframework.boot.SpringApplication;

public class TestSeleniumCodesApplication {

	public static void main(String[] args) {
		SpringApplication.from(SeleniumCodesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
