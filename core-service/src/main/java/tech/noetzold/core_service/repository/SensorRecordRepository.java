package tech.noetzold.core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.noetzold.core_service.model.SensorRecord;

public interface SensorRecordRepository extends JpaRepository<SensorRecord, Long> {
}
