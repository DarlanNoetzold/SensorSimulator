package tech.noetzold.processor_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.noetzold.processor_service.model.Prediction;
import tech.noetzold.processor_service.service.DataService;

@RestController
@RequestMapping("/prediction")
public class PredictionController {

    @Autowired
    private DataService dataService;

    @PostMapping("/process")
    public ResponseEntity<String> processPrediction(@RequestBody Prediction prediction) {
        try {
            dataService.saveRawData(prediction);

            return new ResponseEntity<>("Prediction processed and sent to RabbitMQ!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error processing prediction: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
