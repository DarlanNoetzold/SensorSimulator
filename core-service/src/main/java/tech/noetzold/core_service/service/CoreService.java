package tech.noetzold.core_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.noetzold.core_service.model.SensorRecord;
import tech.noetzold.core_service.repository.SensorRecordRepository;

@Service
public class CoreService {

    @Autowired
    private SensorRecordRepository sensorRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = "sensorDataProcessed")
    public void handleMessage(String message) {
        try {
            // Desserializar a mensagem para Prediction
            SensorRecord sensorRecord = objectMapper.readValue(message, SensorRecord.class);

            // Salvar no banco de dados
            sensorRecordRepository.save(sensorRecord);

            System.out.println("Saved SensorRecord for Sensor: " + sensorRecord.getSensorName());
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }
}
