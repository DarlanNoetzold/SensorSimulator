package tech.noetzold.production_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tech.noetzold.production_service.model.Prediction;
import tech.noetzold.production_service.repository.PredictionRepository;

@Service
public class ProductionService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(fixedRate = 300000) // 5 minutos em milissegundos
    public void sendPredictionsToQueue() {
        Iterable<Prediction> predictions = predictionRepository.findAll();

        for (Prediction prediction : predictions) {
            try {
                String jsonPrediction = objectMapper.writeValueAsString(prediction);

                rabbitTemplate.convertAndSend("productionQueue", jsonPrediction);
                System.out.println("Sent prediction: " + prediction.getSensorName());

                predictionRepository.delete(prediction);
                System.out.println("Deleted prediction: " + prediction.getSensorName());

            } catch (Exception e) {
                System.err.println("Error serializing and sending prediction: " + e.getMessage());
            }
        }
    }
}
