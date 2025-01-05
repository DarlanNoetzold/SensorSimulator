package tech.noetzold.core_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.noetzold.core_service.service.MetricsConsumerService;

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
