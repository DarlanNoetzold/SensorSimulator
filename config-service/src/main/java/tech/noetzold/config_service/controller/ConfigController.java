package tech.noetzold.config_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tech.noetzold.config_service.model.SensorConfig;
import tech.noetzold.config_service.service.ConfigService;

import java.util.List;

@RestController
@RequestMapping("/config-service")
public class ConfigController {

    private final ConfigService configService;
    private final RestTemplate restTemplate;

    @Value("${prediction-service.url}")
    private String predictionServiceUrl;

    public ConfigController(ConfigService configService, RestTemplate restTemplate) {
        this.configService = configService;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadConfiguration(
            @RequestParam("sensorName") String sensorName,
            @RequestParam("unit") String unit,
            @RequestParam("p") Integer p,
            @RequestParam("d") Integer d,
            @RequestParam("q") Integer q,
            @RequestParam("numPredictions") Integer numPredictions,
            @RequestParam("interval") Integer interval,
            @RequestParam(value = "file") MultipartFile file
    ) {
        try {
            // Save configuration localmente, se necessário
            SensorConfig config = new SensorConfig(sensorName, unit, p, d, q, numPredictions, interval);
            configService.saveConfiguration(config);

            // Encaminhar a configuração e o arquivo para o Prediction-Service
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("sensor_name", sensorName);
            body.add("unit", unit);
            body.add("p", p);
            body.add("d", d);
            body.add("q", q);
            body.add("num_predictions", numPredictions);
            body.add("interval", interval);
            body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    predictionServiceUrl + "/train",
                    requestEntity,
                    String.class
            );

            return ResponseEntity.ok("Configuração enviada com sucesso: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao processar a configuração: " + e.getMessage());
        }
    }

    @GetMapping("/configurations")
    public List<SensorConfig> getAllConfigs() {
        return configService.getAllConfigurations();
    }
}