package tech.noetzold.Capture_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.noetzold.Capture_service.service.CaptureService;
import tech.noetzold.Capture_service.model.Prediction;

@RestController
@RequestMapping("/capture")
public class CaptureController {

    @Autowired
    private CaptureService captureService;

    @PostMapping("/process")
    public String processMessage(@RequestBody Prediction prediction) {
        captureService.processPrediction(prediction);

        return "Prediction processed and sent to sensorDataCaptured queue successfully!";
    }
}