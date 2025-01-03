package tech.noetzold.core_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.noetzold.core_service.model.Metrics;
import tech.noetzold.core_service.repository.MetricsRepository;

import java.util.List;

@RequestMapping("/api/metrics")
public class MetricsController {
    @Autowired
    private MetricsRepository metricsRepository;

    // Endpoint para buscar todas as métricas
    @GetMapping
    public ResponseEntity<List<Metrics>> getAllMetrics() {
        List<Metrics> metrics = metricsRepository.findAll();
        return ResponseEntity.ok(metrics);
    }

    // Endpoint para buscar as últimas X métricas
    @GetMapping("/latest")
    public ResponseEntity<List<Metrics>> getLatestMetrics(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<Metrics> metrics = metricsRepository.findTopByOrderByIdDesc(limit);
        return ResponseEntity.ok(metrics);
    }
}
