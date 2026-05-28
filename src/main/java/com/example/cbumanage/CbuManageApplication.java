package com.example.cbumanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication(exclude = RedisRepositoriesAutoConfiguration.class)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class CbuManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(CbuManageApplication.class, args);
	}

}
