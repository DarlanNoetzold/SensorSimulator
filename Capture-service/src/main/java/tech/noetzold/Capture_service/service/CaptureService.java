package tech.noetzold.Capture_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.noetzold.Capture_service.model.Metrics;
import tech.noetzold.Capture_service.model.Prediction;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Random;


@Service
public class CaptureService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${sensor.processor.id}")
    private String sensorProcessorId;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(CaptureService.class);

    public void processPrediction(Prediction prediction) {

        String networkStatus = calculateNetworkStatus(prediction);

        sendToQueue(prediction);
    }

    private Double calculateEnergyUsage(Prediction prediction) {
        return prediction.getValue() * 0.1;
    }

    private String calculateNetworkStatus(Prediction prediction) {
        if (prediction.getValue() > 50) {
            return "High Load";
        } else {
            return "Normal";
        }
    }

    private void sendToQueue(Prediction prediction) {
        try {
            // Serializar o objeto Prediction para JSON
            String jsonPrediction = objectMapper.writeValueAsString(prediction);
            System.out.println("Serialized Prediction to JSON: " + jsonPrediction);
            // Enviar a mensagem JSON para a fila RabbitMQ
            rabbitTemplate.convertAndSend("sensorDataCaptured", jsonPrediction);
            System.out.println("Sent processed prediction to sensorDataCaptured: " + prediction.getSensorName());
        } catch (Exception e) {
            System.err.println("Error sending message to sensorDataCaptured queue: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 10000)
    public void saveMetrics() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        double cpuLoad = osBean.getSystemLoadAverage();
        long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        int threadCount = threadMXBean.getThreadCount();


        Metrics metrics = new Metrics();
        metrics.setCpuUsage(cpuLoad);
        metrics.setMemoryUsage(memoryUsage);
        metrics.setThreadCount(threadCount);
        metrics.setVarianceMap(null);
        metrics.setTotalDataReceived(0L);
        metrics.setTotalDataFiltered(0L);
        metrics.setTotalDataCompressed(0L);
        metrics.setTotalDataAggregated(0L);
        metrics.setTotalDataAfterHeuristics(0L);
        metrics.setErrorCount(0L);
        metrics.setProcessorId(sensorProcessorId);


        sendMetricsToRabbitMQ(metrics);
    }

    private void sendMetricsToRabbitMQ(Metrics metrics) {
        try {
            // Serializar o objeto Metrics para JSON
            String jsonMetrics = objectMapper.writeValueAsString(metrics);
            rabbitTemplate.convertAndSend("metrics", jsonMetrics);
            logger.info("Metrics sent to RabbitMQ");
        } catch (Exception e) {
            logger.error("Error serializing and sending metrics: ", e);
        }
    }

    private long generateRandomValue(long baseValue) {
        Random random = new Random();
        return baseValue > 0 ? baseValue - random.nextInt((int) Math.min(baseValue, 5)) : 0;
    }
}
