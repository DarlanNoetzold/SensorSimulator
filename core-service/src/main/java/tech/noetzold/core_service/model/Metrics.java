package tech.noetzold.core_service.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Map;

@Entity
public class Metrics implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double cpuUsage;
    private long memoryUsage;
    private int threadCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sensor_variance", joinColumns = @JoinColumn(name = "metrics_id"))
    @MapKeyColumn(name = "sensor_type")
    @Column(name = "variance")
    private Map<String, Double> varianceMap;

    private long totalDataReceived;
    private long totalDataFiltered;
    private long totalDataCompressed;
    private long totalDataAggregated;
    private long totalDataAfterHeuristics;
    private long errorCount;
    private String processorId;


    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public Map<String, Double> getVarianceMap() {
        return varianceMap;
    }

    public void setVarianceMap(Map<String, Double> varianceMap) {
        this.varianceMap = varianceMap;
    }

    public long getTotalDataReceived() {
        return totalDataReceived;
    }

    public void setTotalDataReceived(long totalDataReceived) {
        this.totalDataReceived = totalDataReceived;
    }

    public long getTotalDataFiltered() {
        return totalDataFiltered;
    }

    public void setTotalDataFiltered(long totalDataFiltered) {
        this.totalDataFiltered = totalDataFiltered;
    }

    public long getTotalDataCompressed() {
        return totalDataCompressed;
    }

    public void setTotalDataCompressed(long totalDataCompressed) {
        this.totalDataCompressed = totalDataCompressed;
    }

    public long getTotalDataAggregated() {
        return totalDataAggregated;
    }

    public void setTotalDataAggregated(long totalDataAggregated) {
        this.totalDataAggregated = totalDataAggregated;
    }

    public long getTotalDataAfterHeuristics() {
        return totalDataAfterHeuristics;
    }

    public void setTotalDataAfterHeuristics(long totalDataAfterHeuristics) {
        this.totalDataAfterHeuristics = totalDataAfterHeuristics;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }
}