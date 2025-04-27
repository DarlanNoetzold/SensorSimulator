package tech.noetzold.core_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @GetMapping("/{sensorName}")
    public ResponseEntity<List<SensorRecord>> getSensorData(
            @PathVariable String sensorName,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(0, limit);
        Page<SensorRecord> sensorRecords = sensorRecordRepository.findBySensorNameOrderByPredictedDateDesc(sensorName, pageable);

        return ResponseEntity.ok(sensorRecords.getContent());
    }

    @GetMapping
    public ResponseEntity<List<SensorRecord>> getAllSensorsData(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        Pageable pageable = PageRequest.of(0, limit);
        Page<SensorRecord> sensorRecords = sensorRecordRepository.findAllByOrderByPredictedDateDesc(pageable);

        return ResponseEntity.ok(sensorRecords.getContent());
    }
}

