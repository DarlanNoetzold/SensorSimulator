package tech.noetzold.core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.noetzold.core_service.model.Metrics;

import java.util.List;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, Long> {

    // Método para buscar as últimas X métricas, ordenadas pela ID de forma decrescente
    List<Metrics> findTopByOrderByIdDesc(int limit);
}
