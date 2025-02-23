package com.project.foradhd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ForadhdApplication {

	public static void main(String[] args) {
		SpringApplication.run(ForadhdApplication.class, args);
	}

}
