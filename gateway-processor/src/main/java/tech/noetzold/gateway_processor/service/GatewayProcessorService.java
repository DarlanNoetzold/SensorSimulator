package tech.noetzold.gateway_processor.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import tech.noetzold.gateway_processor.model.Prediction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GatewayProcessorService {

    @Autowired
    private DockerService dockerService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private List<String> processorServiceNodes = new ArrayList<>();
    private List<Integer> processorServicePorts = new ArrayList<>();
    private int currentNodeIndex = 0;

    private static final int MIN_NODES = 3;
    private static final int MAX_NODES = 10;
    private static final long NODE_INACTIVITY_TIMEOUT = 30 * 60 * 1000;

    private Map<String, Long> nodeLastUsedTime = new ConcurrentHashMap<>();

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 10000;

    @RabbitListener(queues = "sensorDataCaptured")
    public void handleMessage(String message) {
        if (processorServiceNodes.size() < MIN_NODES) {
            createNewNode();
        }

        String selectedNode = processorServiceNodes.get(currentNodeIndex);
        int selectedPort = processorServicePorts.get(currentNodeIndex);
        currentNodeIndex = (currentNodeIndex + 1) % processorServiceNodes.size();

        sendMessageToNodeWithRetry(selectedNode, selectedPort, message);

        if (processorServiceNodes.size() < MAX_NODES && shouldScaleUp()) {
            createNewNode();
        }
    }

    private void createNewNode() {
        String nodeName = generateUniqueNodeName();

        int nodePort = 10000 + processorServiceNodes.size();
        String containerId = dockerService.createProcessorServiceNode(nodeName, nodePort);

        if (containerId != null) {
            processorServiceNodes.add(nodeName);
            processorServicePorts.add(nodePort);
            nodeLastUsedTime.put(nodeName, System.currentTimeMillis());
        }
    }

    private String generateUniqueNodeName() {
        int nodeCount = processorServiceNodes.size();
        String nodeName = "processor-service-node-" + nodeCount;

        while (processorServiceNodes.contains(nodeName)) {
            nodeCount++;
            nodeName = "processor-service-node-" + nodeCount;
        }

        return nodeName;
    }

    private boolean shouldScaleUp() {
        return processorServiceNodes.size() < MIN_NODES;
    }

    public void removeProcessorNode(String nodeName) {
        int index = processorServiceNodes.indexOf(nodeName);
        if (index != -1) {
            processorServiceNodes.remove(index);
            processorServicePorts.remove(index);
            dockerService.removeProcessorServiceNode(nodeName);
            nodeLastUsedTime.remove(nodeName);
            System.out.println("Removed node: " + nodeName);
        } else {
            System.out.println("Node not found: " + nodeName);
        }

        if (currentNodeIndex >= processorServiceNodes.size()) {
            currentNodeIndex = 0;
        }
    }

    // Método para enviar a mensagem ao nó Processor-Service via HTTP com retentativas
    private void sendMessageToNodeWithRetry(String nodeName, int nodePort, String message) {
        int retries = 0;

        while (retries < MAX_RETRIES) {
            try {
                Prediction prediction = objectMapper.readValue(message, Prediction.class);
                prediction.setId(null);

                String jsonPrediction = objectMapper.writeValueAsString(prediction);

                String url = "http://127.0.0.1:" + nodePort + "/prediction/process";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> request = new HttpEntity<>(jsonPrediction, headers);

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Message successfully sent to node " + nodeName);
                    nodeLastUsedTime.put(nodeName, System.currentTimeMillis());
                    return;
                } else {
                    System.out.println("Failed to send message to node " + nodeName + ": " + response.getStatusCode());
                }
            } catch (Exception e) {
                System.out.println("Failed to send message to node " + nodeName + ": " + e.getMessage());
            }

            retries++;
            if (retries < MAX_RETRIES) {
                System.out.println("Retrying... Attempt " + (retries + 1) + " of " + MAX_RETRIES);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    System.err.println("Retry sleep interrupted: " + ie.getMessage());
                }
            } else {
                System.err.println("Max retries reached. Giving up on sending message to node " + nodeName);
            }
        }
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void checkInactiveNodes() {
        long currentTime = System.currentTimeMillis();
        List<String> inactiveNodes = new ArrayList<>();

        for (String nodeName : processorServiceNodes) {
            if (isNodeInactive(nodeName, currentTime)) {
                inactiveNodes.add(nodeName);
            }
        }

        for (String nodeName : inactiveNodes) {
            removeProcessorNode(nodeName);
        }
    }

    private boolean isNodeInactive(String nodeName, long currentTime) {
        Long lastUsedTime = nodeLastUsedTime.get(nodeName);
        return lastUsedTime != null && (currentTime - lastUsedTime) > NODE_INACTIVITY_TIMEOUT;
    }
}
