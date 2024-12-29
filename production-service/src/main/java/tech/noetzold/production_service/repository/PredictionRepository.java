package tech.noetzold.production_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.noetzold.production_service.model.Prediction;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
}
