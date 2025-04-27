package tech.noetzold.core_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.noetzold.core_service.model.Metrics;
import tech.noetzold.core_service.repository.MetricsRepository;

import java.io.IOException;
import java.util.Map;

@Service
public class MetricsConsumerService {

    private final ObjectMapper objectMapper;
    private final MetricsRepository metricsRepository;

    public MetricsConsumerService(ObjectMapper objectMapper, MetricsRepository metricsRepository) {
        this.objectMapper = objectMapper;
        this.metricsRepository = metricsRepository;
    }

    // Método para processar as mensagens da fila de métricas
    @RabbitListener(queues = "metrics")
    @Transactional
    public void consumeMetricsMessage(String payload) {
        try {
            Map<String, Object> metricsData = objectMapper.readValue(payload, Map.class);

            Metrics metrics = convertMapToMetrics(metricsData);

            metricsRepository.save(metrics);

            System.out.println("Metrics saved successfully: " + metrics);
        } catch (IOException e) {
            System.err.println("Error while deserializing message: " + e.getMessage());
            throw new org.springframework.amqp.AmqpRejectAndDontRequeueException("Message format invalid", e);
        }
    }

    // Método auxiliar para converter o Map para o modelo Metrics
    private Metrics convertMapToMetrics(Map<String, Object> metricsData) {
        Metrics metrics = new Metrics();
        metrics.setCpuUsage((Double) metricsData.get("cpuUsage"));

        metrics.setMemoryUsage(((Integer) metricsData.get("memoryUsage")).longValue());

        metrics.setThreadCount((Integer) metricsData.get("threadCount"));

        metrics.setVarianceMap((Map<String, Double>) metricsData.get("varianceMap"));

        metrics.setTotalDataReceived(((Integer) metricsData.get("totalDataReceived")).longValue());
        metrics.setTotalDataFiltered(((Integer) metricsData.get("totalDataFiltered")).longValue());
        metrics.setTotalDataCompressed(((Integer) metricsData.get("totalDataCompressed")).longValue());
        metrics.setTotalDataAggregated(((Integer) metricsData.get("totalDataAggregated")).longValue());
        metrics.setTotalDataAfterHeuristics(((Integer) metricsData.get("totalDataAfterHeuristics")).longValue());

        metrics.setErrorCount(((Integer) metricsData.get("errorCount")).longValue());

        metrics.setProcessorId((String) metricsData.get("processorId"));

        return metrics;
    }
}
