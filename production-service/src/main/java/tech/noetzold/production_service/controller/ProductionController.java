package tech.noetzold.production_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.noetzold.production_service.service.ProductionService;

@RestController
@RequestMapping("/production")
public class ProductionController {

    @Autowired
    private ProductionService productionService;

    @PostMapping("/send")
    public String sendPredictions() {
        productionService.sendPredictionsToQueue();
        return "Predictions sent to the queue!";
    }
}
