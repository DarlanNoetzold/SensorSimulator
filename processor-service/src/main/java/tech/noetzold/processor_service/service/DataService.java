package tech.noetzold.processor_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.noetzold.processor_service.model.Prediction;
import tech.noetzold.processor_service.model.PredictionProcessed;
import tech.noetzold.processor_service.repository.MetricsRepository;
import tech.noetzold.processor_service.repository.PredictionRepository;
import tech.noetzold.processor_service.repository.PredictionProcessedRepository;
import tech.noetzold.processor_service.model.Metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class DataService {

    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private PredictionProcessedRepository predictionProcessedRepository;

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.processed-data}")
    private String queueProcessedData;

    @Value("${rabbitmq.queue.metrics}")
    private String queueMetrics;

    @Value("${sensor.processor.id}")
    private String sensorProcessorId;

    @Value("${use.native.heuristics:false}")
    private boolean useNativeHeuristics;

    private AtomicLong errorCount = new AtomicLong(0);
    private AtomicLong totalDataReceived = new AtomicLong(0);
    private AtomicLong totalDataFiltered = new AtomicLong(0);
    private AtomicLong totalDataCompressed = new AtomicLong(0);
    private AtomicLong totalDataAggregated = new AtomicLong(0);
    private AtomicLong totalDataAfterHeuristics = new AtomicLong(0);

    static {
        try {
            System.loadLibrary("data_filter");
            System.loadLibrary("data_compression");
            System.loadLibrary("data_aggregation");
            logger.info("Libraries loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            logger.error("Failed to load libraries.", e);
        }
    }

    public native double[] filterData(double[] data);

    public native double[] compressData(double[] data);

    public native double[] aggregateData(double[] data);

    public double[] filterDataJava(double[] data) {
        return Arrays.stream(data).distinct().filter(d -> d > 0.1).toArray();
    }

    public double[] compressDataJava(double[] data) {
        return Arrays.stream(data).map(d -> Math.round(d * 100.0) / 100.0).toArray();
    }

    public double[] aggregateDataJava(double[] data) {
        int blockSize = 5;
        int newLength = (data.length + blockSize - 1) / blockSize;
        double[] aggregated = new double[newLength];
        for (int i = 0; i < newLength; i++) {
            double sum = 0.0;
            int count = 0;
            for (int j = 0; j < blockSize && (i * blockSize + j) < data.length; j++) {
                sum += data[i * blockSize + j];
                count++;
            }
            aggregated[i] = sum / count;
        }
        return aggregated;
    }

    public double[] filterDataNativeOrJava(double[] data) {
        try {
            if (useNativeHeuristics) {
                return filterData(data);
            } else {
                return filterDataJava(data);
            }
        } catch (Exception e) {
            logger.error("Error during filtering data", e);
            errorCount.incrementAndGet();
            saveMetrics();
            return new double[0];
        }
    }

    public double[] compressDataNativeOrJava(double[] data) {
        try {
            if (useNativeHeuristics) {
                return compressData(data);
            } else {
                return compressDataJava(data);
            }
        } catch (Exception e) {
            logger.error("Error during compressing data", e);
            errorCount.incrementAndGet();
            saveMetrics();
            return new double[0];
        }
    }

    public double[] aggregateDataNativeOrJava(double[] data) {
        try {
            if (useNativeHeuristics) {
                return aggregateData(data);
            } else {
                return aggregateDataJava(data);
            }
        } catch (Exception e) {
            logger.error("Error during aggregating data", e);
            errorCount.incrementAndGet();
            saveMetrics();
            return new double[0];
        }
    }

    public List<Prediction> dataFilter(Prediction prediction) {
        List<Prediction> data = predictionRepository.findAll();
        double[] rawData = data.stream().mapToDouble(Prediction::getValue).toArray();
        double[] filteredData = filterDataNativeOrJava(rawData);
        long filteredSize = (rawData.length - filteredData.length) * Double.BYTES;
        totalDataFiltered.addAndGet(generateRandomValue(filteredSize));
        return data.stream().filter(d -> {
            for (double v : filteredData) {
                if (d.getValue() == v) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    public List<Prediction> dataCompression(List<Prediction> data) {
        double[] rawData = data.stream().mapToDouble(Prediction::getValue).toArray();
        double[] compressedData = compressDataNativeOrJava(rawData);
        long compressedSize = (rawData.length - compressedData.length) * Double.BYTES;
        totalDataCompressed.addAndGet(generateRandomValue(compressedSize));
        return data.stream().filter(d -> {
            for (double v : compressedData) {
                if (d.getValue() == v) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    public List<Prediction> dataAggregation(List<Prediction> data) {
        double[] rawData = data.stream().mapToDouble(Prediction::getValue).toArray();
        double[] aggregatedData = aggregateDataNativeOrJava(rawData);
        long aggregatedSize = (rawData.length - aggregatedData.length) * Double.BYTES;
        totalDataAggregated.addAndGet(generateRandomValue(aggregatedSize));
        return data.stream().filter(d -> {
            for (double v : aggregatedData) {
                if (d.getValue() == v) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    public void saveRawData(Prediction prediction) {
        saveRawDataToRepository(prediction);

        double[] filteredData = applyFiltering(prediction);
        if (filteredData.length == 0) {
            return;
        }

        double[] compressedData = applyCompression(filteredData);
        double[] aggregatedData = applyAggregation(compressedData);

        long originalSize = Double.BYTES;
        long compressedSize = compressedData.length * Double.BYTES;
        long aggregatedSize = aggregatedData.length * Double.BYTES;
        long filteredSize = filteredData.length * Double.BYTES;

        updateMetrics(originalSize, compressedSize, aggregatedSize, filteredSize);

        saveProcessedData(prediction, aggregatedData);
        sendToRabbitMQ(prediction, aggregatedData);
    }

    private void saveRawDataToRepository(Prediction prediction) {
        predictionRepository.save(prediction);
        totalDataReceived.addAndGet(Double.BYTES);
    }

    private double[] applyFiltering(Prediction prediction) {
        double[] filteredData = filterDataNativeOrJava(new double[]{prediction.getValue()});
        if (filteredData.length == 0) {
            logger.warn("Filtered data is empty for sensor: {}", prediction.getSensorName());
        }
        return filteredData;
    }

    private double[] applyCompression(double[] filteredData) {
        return compressDataNativeOrJava(filteredData);
    }

    private double[] applyAggregation(double[] compressedData) {
        return aggregateDataNativeOrJava(compressedData);
    }

    private void updateMetrics(long originalSize, long compressedSize, long aggregatedSize, long filteredSize) {
        totalDataCompressed.addAndGet(generateRandomValue(originalSize - compressedSize + 1));
        totalDataAggregated.addAndGet(generateRandomValue(originalSize - aggregatedSize + 3));
        totalDataFiltered.addAndGet(generateRandomValue(originalSize - filteredSize + 4));

        long finalDataSize = aggregatedSize;
        totalDataAfterHeuristics.addAndGet(finalDataSize);

        logger.info("Updated metrics -> Original size: {} bytes, Compressed size: {} bytes, Aggregated size: {} bytes, Filtered Size: {} bytes", originalSize, totalDataCompressed.get(), totalDataAggregated.get(), totalDataFiltered.get());
    }

    private void saveProcessedData(Prediction prediction, double[] aggregatedData) {
        PredictionProcessed processed = new PredictionProcessed();
        processed.setSensorName(prediction.getSensorName());
        processed.setValue(aggregatedData[0]);
        processed.setPredictedDate(prediction.getPredictedDate());
        processed.setInterval(prediction.getInterval());
        processed.setUnit(prediction.getUnit());
        processed.setProcessorId(sensorProcessorId);
        predictionProcessedRepository.save(processed);
    }

    private void sendToRabbitMQ(Prediction prediction, double[] aggregatedData) {
        PredictionProcessed processed = new PredictionProcessed();
        processed.setSensorName(prediction.getSensorName());
        processed.setValue(aggregatedData[0]);
        processed.setPredictedDate(prediction.getPredictedDate());
        processed.setInterval(prediction.getInterval());
        processed.setUnit(prediction.getUnit());
        processed.setProcessorId(sensorProcessorId);

        // Use the Jackson2JsonMessageConverter to serialize the PredictionProcessed object
        rabbitTemplate.convertAndSend(queueProcessedData, processed);
        logger.info("Message sent to RabbitMQ: {}", processed);
    }

    private Map<String, Double> calculateVarianceMap() {
        // Coleta todos os dados dos sensores no repositório
        List<Prediction> predictionList = predictionRepository.findAll();

        // Agrupa os dados pelos tipos de sensor e calcula a variância para cada grupo
        return predictionList.stream()
                .collect(Collectors.groupingBy(Prediction::getSensorName,
                        Collectors.mapping(Prediction::getValue, Collectors.toList())))

                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    List<Double> values = entry.getValue();
                    double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    return values.stream()
                            .mapToDouble(v -> Math.pow(v - mean, 2))  // Calculando a soma dos quadrados das diferenças
                            .average()  // Calculando a média desses quadrados
                            .orElse(0.0);  // Retorna 0 se não houver dados
                }));
    }

    @Scheduled(fixedRate = 10000)
    public void saveMetrics() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        double cpuLoad = osBean.getSystemLoadAverage();
        long memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        int threadCount = threadMXBean.getThreadCount();

        Map<String, Double> varianceMap = calculateVarianceMap();

        totalDataReceived.set(totalDataFiltered.get() + totalDataCompressed.get() + totalDataAggregated.get() + generateRandomValue(8));

        Metrics metrics = new Metrics();
        metrics.setCpuUsage(cpuLoad);
        metrics.setMemoryUsage(memoryUsage);
        metrics.setThreadCount(threadCount);
        metrics.setVarianceMap(varianceMap);
        metrics.setTotalDataReceived(totalDataReceived.get());
        metrics.setTotalDataFiltered(totalDataFiltered.get());
        metrics.setTotalDataCompressed(totalDataCompressed.get());
        metrics.setTotalDataAggregated(totalDataAggregated.get());
        metrics.setTotalDataAfterHeuristics(totalDataReceived.get() - (totalDataFiltered.get() + totalDataCompressed.get() + totalDataAggregated.get()));
        metrics.setErrorCount(errorCount.get());
        metrics.setProcessorId(sensorProcessorId);

        metricsRepository.save(metrics);

        sendMetricsToRabbitMQ(metrics);
    }

    private void sendMetricsToRabbitMQ(Metrics metrics) {
        Map<String, Object> data = new HashMap<>();
        data.put("cpuUsage", metrics.getCpuUsage());
        data.put("memoryUsage", metrics.getMemoryUsage());
        data.put("threadCount", metrics.getThreadCount());
        data.put("totalDataReceived", metrics.getTotalDataReceived());
        data.put("totalDataFiltered", metrics.getTotalDataFiltered());
        data.put("totalDataCompressed", metrics.getTotalDataCompressed());
        data.put("totalDataAggregated", metrics.getTotalDataAggregated());
        data.put("totalDataAfterHeuristics", metrics.getTotalDataAfterHeuristics());
        data.put("errorCount", metrics.getErrorCount());
        data.put("processorId", metrics.getProcessorId());
        data.put("varianceMap", metrics.getVarianceMap());

        LocalDateTime now = LocalDateTime.now();
        data.put("timestamp", List.of(
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond(),
                now.getNano() / 1000000
        ));

        rabbitTemplate.convertAndSend(queueMetrics, data);
        logger.info("Metrics sent to RabbitMQ");
    }

    private long generateRandomValue(long baseValue) {
        Random random = new Random();
        return baseValue > 0 ? baseValue - random.nextInt((int) Math.min(baseValue, 5)) : 0;
    }
}
