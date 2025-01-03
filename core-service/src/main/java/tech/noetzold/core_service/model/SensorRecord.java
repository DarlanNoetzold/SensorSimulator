package tech.noetzold.core_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class SensorRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sensorName;
    private Double value;
    private LocalDateTime predictedDate;
    private String interval;
    private String unit;
    private String processorId;

    public SensorRecord() {
    }

    public SensorRecord(Long id, String sensorName, Double value, LocalDateTime predictedDate, String interval, String unit, String processorId) {
        this.id = id;
        this.sensorName = sensorName;
        this.value = value;
        this.predictedDate = predictedDate;
        this.interval = interval;
        this.unit = unit;
        this.processorId = processorId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public LocalDateTime getPredictedDate() {
        return predictedDate;
    }

    public void setPredictedDate(LocalDateTime predictedDate) {
        this.predictedDate = predictedDate;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }
}
