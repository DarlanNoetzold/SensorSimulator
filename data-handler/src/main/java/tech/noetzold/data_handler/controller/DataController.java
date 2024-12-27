package tech.noetzold.data_handler.controller;

import org.springframework.web.bind.annotation.*;
import tech.noetzold.data_handler.model.Prediction;
import tech.noetzold.data_handler.service.DataService;

import java.util.List;

@RestController
@RequestMapping("/data-handler")
public class DataController {

    private final DataService dataService;

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/historical-data")
    public List<Prediction> getHistoricalData() {
        return dataService.getAllPredictions();
    }

    @GetMapping("/sensor-data/{sensorName}")
    public List<Prediction> getSensorData(@PathVariable String sensorName) {
        return dataService.getPredictionsBySensorName(sensorName);
    }
}