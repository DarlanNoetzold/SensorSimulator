package tech.noetzold.processor_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.noetzold.processor_service.model.Metrics;

@Repository
public interface MetricsRepository  extends JpaRepository<Metrics, Long> {
}
