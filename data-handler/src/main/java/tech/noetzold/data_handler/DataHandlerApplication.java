package tech.noetzold.data_handler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
public class DataHandlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataHandlerApplication.class, args);
	}

}
