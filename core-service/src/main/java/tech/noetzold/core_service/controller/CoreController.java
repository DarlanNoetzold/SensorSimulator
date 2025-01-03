package tech.noetzold.core_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.noetzold.core_service.model.SensorRecord;
import tech.noetzold.core_service.repository.SensorRecordRepository;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class CoreController {

    @Autowired
    private SensorRecordRepository sensorRecordRepository;

    // Endpoint para buscar dados de um sensor pelo nome
    @GetMapping("/{sensorName}")
    public ResponseEntity<List<SensorRecord>> getSensorData(
            @PathVariable String sensorName,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(0, limit); // Criação do Pageable com o limite
        List<SensorRecord> sensorRecords = sensorRecordRepository.findTopBySensorNameOrderByPredictedDateDesc(sensorName, pageable);
        return ResponseEntity.ok(sensorRecords);
    }

    // Endpoint para buscar dados de todos os sensores
    @GetMapping
    public ResponseEntity<List<SensorRecord>> getAllSensorsData(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(0, limit); // Criação do Pageable com o limite
        List<SensorRecord> sensorRecords = sensorRecordRepository.findTopByOrderByPredictedDateDesc(pageable);
        return ResponseEntity.ok(sensorRecords);
    }
}
