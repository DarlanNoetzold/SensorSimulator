package tech.noetzold.core_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.noetzold.core_service.model.Metrics;
import tech.noetzold.core_service.repository.MetricsRepository;

import java.util.List;

@RestController
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

    // Endpoint para buscar as últimas métricas com um limite
    @GetMapping("/latest")
    public ResponseEntity<List<Metrics>> getLatestMetrics(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        // Aplica o limite no Pageable
        Pageable pageable = PageRequest.of(0, limit);

        // Busca as últimas métricas
        List<Metrics> metrics = metricsRepository.findAll(pageable).getContent();
        return ResponseEntity.ok(metrics);
    }
}
