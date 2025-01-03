package tech.noetzold.core_service.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.noetzold.core_service.model.Metrics;
import tech.noetzold.core_service.repository.MetricsRepository;

import java.util.Map;

@Service
public class MetricsConsumerService {

    @Autowired
    private MetricsRepository metricsRepository;

    @RabbitListener(queues = "metrics")
    public void consumeMetricsMessage(Map<String, Object> metricsData) {
        try {
            // Aqui, você pode transformar a mensagem recebida em um objeto Metrics.
            Metrics metrics = new Metrics();
            metrics.setCpuUsage((Double) metricsData.get("cpuUsage"));
            metrics.setMemoryUsage((Long) metricsData.get("memoryUsage"));
            metrics.setThreadCount((Integer) metricsData.get("threadCount"));
            metrics.setTotalDataReceived((Long) metricsData.get("totalDataReceived"));
            metrics.setTotalDataFiltered((Long) metricsData.get("totalDataFiltered"));
            metrics.setTotalDataCompressed((Long) metricsData.get("totalDataCompressed"));
            metrics.setTotalDataAggregated((Long) metricsData.get("totalDataAggregated"));
            metrics.setTotalDataAfterHeuristics((Long) metricsData.get("totalDataAfterHeuristics"));
            metrics.setErrorCount((Long) metricsData.get("errorCount"));
            metrics.setProcessorId((String) metricsData.get("processorId"));
            metrics.setVarianceMap((Map<String, Double>) metricsData.get("varianceMap"));

            // Salvar a métrica recebida no banco de dados
            metricsRepository.save(metrics);
            System.out.println("Metrics data saved successfully!");

        } catch (Exception e) {
            System.err.println("Error processing metrics message: " + e.getMessage());
        }
    }
}
