package tech.noetzold.data_handler.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.noetzold.data_handler.model.PredictionStatistics;
import tech.noetzold.data_handler.service.StatisticsService;

import java.util.List;

@RestController
@RequestMapping("/data-handler")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    public List<PredictionStatistics> getStatistics() {
        return statisticsService.getAllStatistics();
    }
}
