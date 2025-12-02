package com.example.cbumanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class CbuManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(CbuManageApplication.class, args);
	}

}
