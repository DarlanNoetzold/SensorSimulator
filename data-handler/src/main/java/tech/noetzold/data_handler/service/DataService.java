package tech.noetzold.data_handler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.noetzold.data_handler.model.Prediction;
import tech.noetzold.data_handler.repository.PredictionRepository;

import java.util.List;

@Component
public class DataService {

    @Autowired
    private PredictionRepository predictionRepository;

    public DataService() {

    }

    public List<Prediction> getAllPredictions() {
        return predictionRepository.findAll();
    }

    public List<Prediction> getPredictionsBySensorName(String sensorName) {
        return predictionRepository.findBySensorName(sensorName);
    }
}
