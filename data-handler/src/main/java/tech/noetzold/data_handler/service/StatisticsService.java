package tech.noetzold.data_handler.service;

import org.springframework.stereotype.Service;
import tech.noetzold.data_handler.model.PredictionStatistics;
import tech.noetzold.data_handler.repository.PredictionStatisticsRepository;

import java.util.List;

@Service
public class StatisticsService {

    private final PredictionStatisticsRepository statisticsRepository;

    public StatisticsService(PredictionStatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public List<PredictionStatistics> getAllStatistics() {
        return statisticsRepository.findAll();
    }
}
