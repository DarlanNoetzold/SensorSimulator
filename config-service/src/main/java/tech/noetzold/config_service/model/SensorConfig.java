package tech.noetzold.config_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sensor_configurations")
public class SensorConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sensor_name", nullable = false)
    private String sensorName;

    @Column(name = "unit", nullable = false)
    private String unit;

    private Integer p;
    private Integer d;
    private Integer q;

    @Column(name = "num_predictions")
    private Integer numPredictions;

    private Integer interval;

    public SensorConfig() {
    }

    public SensorConfig(String sensorName, String unit, Integer p, Integer d, Integer q, Integer numPredictions, Integer interval) {
        this.sensorName = sensorName;
        this.unit = unit;
        this.p = p;
        this.d = d;
        this.q = q;
        this.numPredictions = numPredictions;
        this.interval = interval;
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

    public Integer getP() {
        return p;
    }

    public void setP(Integer p) {
        this.p = p;
    }

    public Integer getD() {
        return d;
    }

    public void setD(Integer d) {
        this.d = d;
    }

    public Integer getQ() {
        return q;
    }

    public void setQ(Integer q) {
        this.q = q;
    }

    public Integer getNumPredictions() {
        return numPredictions;
    }

    public void setNumPredictions(Integer numPredictions) {
        this.numPredictions = numPredictions;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }
}