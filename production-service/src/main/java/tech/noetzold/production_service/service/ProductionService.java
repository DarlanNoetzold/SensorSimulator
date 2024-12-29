package tech.noetzold.production_service.service;

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

    // Enviar predições a cada 5 minutos
    @Scheduled(fixedRate = 300000) // 5 minutos em milissegundos
    public void sendPredictionsToQueue() {
        Iterable<Prediction> predictions = predictionRepository.findAll();

        for (Prediction prediction : predictions) {
            // Enviar cada predição para a fila RabbitMQ
            rabbitTemplate.convertAndSend("productionQueue", prediction);
            System.out.println("Sent prediction: " + prediction.getSensorName());
        }
    }
}
