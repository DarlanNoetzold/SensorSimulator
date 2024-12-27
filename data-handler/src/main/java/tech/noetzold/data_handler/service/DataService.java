package tech.noetzold.data_handler.service;

import org.springframework.stereotype.Service;
import tech.noetzold.data_handler.model.Prediction;
import tech.noetzold.data_handler.repository.PredictionRepository;

import java.util.List;

@Service
public class DataService {

    private final PredictionRepository predictionRepository;

    public DataService(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    public List<Prediction> getAllPredictions() {
        return predictionRepository.findAll();
    }

    public List<Prediction> getPredictionsBySensorName(String sensorName) {
        return predictionRepository.findBySensorName(sensorName);
    }
}
