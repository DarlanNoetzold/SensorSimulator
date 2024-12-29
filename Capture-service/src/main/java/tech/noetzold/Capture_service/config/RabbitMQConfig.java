package tech.noetzold.Capture_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue sensorDataCapturedQueue() {
        return new Queue("sensorDataCaptured", false);
    }
}