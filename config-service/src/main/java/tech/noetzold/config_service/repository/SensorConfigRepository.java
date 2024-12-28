package tech.noetzold.config_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.noetzold.config_service.model.SensorConfig;

@Repository
public interface SensorConfigRepository extends JpaRepository<SensorConfig, Long> {
}

