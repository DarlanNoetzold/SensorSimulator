package tech.noetzold.data_handler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.noetzold.data_handler.model.Prediction;
import tech.noetzold.data_handler.service.PredictionService;

import java.util.List;

@RestController
@RequestMapping("/data-handler")
public class DataController {

    @Autowired
    private PredictionService predictionService;

    @GetMapping("/predictions")
    public List<Prediction> getAllPredictions() {
        return predictionService.getAllPredictions();
    }

    @GetMapping("/predictions/{sensorName}")
    public List<Prediction> getPredictionsBySensorName(@PathVariable String sensorName) {
        return predictionService.getPredictionsBySensorName(sensorName);
    }
}
