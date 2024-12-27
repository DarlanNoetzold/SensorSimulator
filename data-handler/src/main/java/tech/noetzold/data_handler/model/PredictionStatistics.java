package tech.noetzold.data_handler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "prediction_statistics")
public class PredictionStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "prediction_id")
    private Prediction prediction;

    private Double mean;

    @Column(name = "standard_deviation")
    private Double standardDeviation;

    private Double variance;

    public PredictionStatistics() {
    }

    public PredictionStatistics(Prediction prediction, Double mean, Double standardDeviation, Double variance) {
        this.prediction = prediction;
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.variance = variance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public Double getMean() {
        return mean;
    }

    public void setMean(Double mean) {
        this.mean = mean;
    }

    public Double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(Double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public Double getVariance() {
        return variance;
    }

    public void setVariance(Double variance) {
        this.variance = variance;
    }

    // Getters and Setters
}