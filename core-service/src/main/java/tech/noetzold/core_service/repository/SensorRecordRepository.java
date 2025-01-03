package tech.noetzold.core_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.noetzold.core_service.model.SensorRecord;

import java.util.List;

@Repository
public interface SensorRecordRepository extends JpaRepository<SensorRecord, Long> {

    // Método para buscar os dados de um sensor específico, limitados e ordenados pela data (do mais recente para o mais antigo)
    Page<SensorRecord> findBySensorNameOrderByPredictedDateDesc(String sensorName, Pageable pageable);

    // Método para buscar todos os dados, limitados e ordenados pela data (do mais recente para o mais antigo)
    Page<SensorRecord> findAllByOrderByPredictedDateDesc(Pageable pageable);
}

