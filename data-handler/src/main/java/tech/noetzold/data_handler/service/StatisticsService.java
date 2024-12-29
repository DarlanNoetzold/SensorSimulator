package tech.noetzold.data_handler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.noetzold.data_handler.model.PredictionStatistics;
import tech.noetzold.data_handler.repository.PredictionStatisticsRepository;

import java.util.List;

@Service
public class StatisticsService {

    @Autowired
    private PredictionStatisticsRepository statisticsRepository;

    public StatisticsService() {
    }

    public List<PredictionStatistics> getAllStatistics() {
        return statisticsRepository.findAll();
    }
}
