package tech.noetzold.data_handler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.noetzold.data_handler.model.PredictionStatistics;

@Repository
public interface PredictionStatisticsRepository extends JpaRepository<PredictionStatistics, Long> {
}
