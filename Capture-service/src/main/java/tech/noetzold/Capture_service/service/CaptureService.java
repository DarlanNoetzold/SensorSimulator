package tech.noetzold.Capture_service.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.noetzold.Capture_service.model.Prediction;

@Service
public class CaptureService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void processPrediction(Prediction prediction) {

        String networkStatus = calculateNetworkStatus(prediction);

        sendToQueue(prediction, networkStatus);
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

    private void sendToQueue(Prediction prediction, String networkStatus) {
        try {
            String message = "Prediction: " + prediction.getSensorName() + ", Value: " + prediction.getValue() + ", Network Status: " + networkStatus;
            rabbitTemplate.convertAndSend("sensorDataCaptured", message);
            System.out.println("Sent processed prediction to sensorDataCaptured: " + prediction.getSensorName());
        } catch (Exception e) {
            System.err.println("Error sending message to sensorDataCaptured queue: " + e.getMessage());
        }
    }
}
