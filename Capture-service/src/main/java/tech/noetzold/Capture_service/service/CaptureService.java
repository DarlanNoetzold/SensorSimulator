package tech.noetzold.Capture_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.noetzold.Capture_service.model.Prediction;

@Service
public class CaptureService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void processPrediction(Prediction prediction) {

        String networkStatus = calculateNetworkStatus(prediction);

        sendToQueue(prediction);
    }

    private Double calculateEnergyUsage(Prediction prediction) {
        return prediction.getValue() * 0.1;
    }

    private String calculateNetworkStatus(Prediction prediction) {
        if (prediction.getValue() > 50) {
            return "High Load";
        } else {
            return "Normal";
        }
    }

    private void sendToQueue(Prediction prediction) {
        try {
            // Serializar o objeto Prediction para JSON
            String jsonPrediction = objectMapper.writeValueAsString(prediction);

            // Enviar a mensagem JSON para a fila RabbitMQ
            rabbitTemplate.convertAndSend("sensorDataCaptured", jsonPrediction);
            System.out.println("Sent processed prediction to sensorDataCaptured: " + prediction.getSensorName());
        } catch (Exception e) {
            System.err.println("Error sending message to sensorDataCaptured queue: " + e.getMessage());
        }
    }
}
