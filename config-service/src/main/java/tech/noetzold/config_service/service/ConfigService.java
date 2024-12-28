package tech.noetzold.config_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.noetzold.config_service.model.SensorConfig;
import tech.noetzold.config_service.repository.SensorConfigRepository;

import java.util.List;

@Service
public class ConfigService {

    @Autowired
    private SensorConfigRepository sensorConfigRepository;

    public ConfigService() {
    }

    public SensorConfig saveConfiguration(SensorConfig config) {
        return sensorConfigRepository.save(config);
    }

    public List<SensorConfig> getAllConfigurations() {
        return sensorConfigRepository.findAll();
    }
}