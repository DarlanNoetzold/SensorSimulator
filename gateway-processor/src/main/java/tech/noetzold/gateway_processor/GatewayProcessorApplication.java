package tech.noetzold.gateway_processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GatewayProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayProcessorApplication.class, args);
	}

}
