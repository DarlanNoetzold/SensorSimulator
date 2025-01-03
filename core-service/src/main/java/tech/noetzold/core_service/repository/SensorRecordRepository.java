package tech.noetzold.core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.noetzold.core_service.model.SensorRecord;

import java.util.List;

public interface SensorRecordRepository extends JpaRepository<SensorRecord, Long> {

    // Método para buscar os dados de um sensor específico, limitados e ordenados pela data (do mais recente para o mais antigo)
    List<SensorRecord> findTopBySensorNameOrderByPredictedDateDesc(String sensorName, int limit);

    // Método para buscar todos os dados, limitados e ordenados pela data (do mais recente para o mais antigo)
    List<SensorRecord> findTopByOrderByPredictedDateDesc(int limit);
}
