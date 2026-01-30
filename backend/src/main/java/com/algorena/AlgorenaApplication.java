package com.algorena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AlgorenaApplication {

	static void main(String[] args) {
		SpringApplication.run(AlgorenaApplication.class, args);
	}

}
