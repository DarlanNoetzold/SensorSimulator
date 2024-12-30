package tech.noetzold.gateway_capturer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GatewayCapturerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayCapturerApplication.class, args);
	}

}
