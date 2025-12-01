// ----------------------------------------------- Package

package com.tailor_pulp_website.back_end;

// ----------------------------------------------- Java Spring Boot

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;




@EnableAsync
@SpringBootApplication
@EnableScheduling
public class MainApplication {

	
	public static void main(String[] args) {
		
		SpringApplication.run(MainApplication.class, args);
	}
}
