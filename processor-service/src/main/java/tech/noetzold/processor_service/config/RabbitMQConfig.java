package tech.noetzold.processor_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue sensorDataProcessedQueue() {
        return new Queue("sensorDataProcessed", true, false, false);
    }

    @Bean
    public Queue metricsQueue() {
        return new Queue("metrics", true, false, false);
    }
}
