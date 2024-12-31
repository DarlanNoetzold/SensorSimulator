package tech.noetzold.processor_service.model;


import java.time.LocalDateTime;

public class Prediction {

    private Long id;

    private String sensorName;

    private String unit;

    private Integer interval;

    private LocalDateTime predictedDate;

    private Double value;

    public Prediction() {
    }

    public Prediction(Long id, String sensorName, String unit, Integer interval, LocalDateTime predictedDate, Double value) {
        this.id = id;
        this.sensorName = sensorName;
        this.unit = unit;
        this.interval = interval;
        this.predictedDate = predictedDate;
        this.value = value;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public LocalDateTime getPredictedDate() {
        return predictedDate;
    }

    public void setPredictedDate(LocalDateTime predictedDate) {
        this.predictedDate = predictedDate;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }



}
