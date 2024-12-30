package tech.noetzold.gateway_capturer.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue productionQueue() {
        return new Queue("productionQueue", true, false, false);
    }
}
