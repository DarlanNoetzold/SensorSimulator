package tech.noetzold.gateway_processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GatewayCapturerService {

    @Autowired
    private DockerService dockerService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private List<String> captureServiceNodes = new ArrayList<>();
    private List<Integer> captureServicePorts = new ArrayList<>();
    private int currentNodeIndex = 0;

    private static final int MIN_NODES = 3;
    private static final int MAX_NODES = 10;
    private static final long NODE_INACTIVITY_TIMEOUT = 5 * 60 * 1000;

    private Map<String, Long> nodeLastUsedTime = new ConcurrentHashMap<>();

    @RabbitListener(queues = "sensorDataCaptured")
    public void handleMessage(String message) {
        if (captureServiceNodes.size() < MIN_NODES) {
            createNewNode();
        }

        String selectedNode = captureServiceNodes.get(currentNodeIndex);
        int selectedPort = captureServicePorts.get(currentNodeIndex);
        currentNodeIndex = (currentNodeIndex + 1) % captureServiceNodes.size();

        sendMessageToNode(selectedNode, selectedPort, message);

        if (captureServiceNodes.size() < MAX_NODES && shouldScaleUp()) {
            createNewNode();
        }
    }

    private void createNewNode() {
        String nodeName = generateUniqueNodeName();

        int nodePort = 9000 + captureServiceNodes.size();
        String containerId = dockerService.createProcessorServiceNode(nodeName, nodePort);

        if (containerId != null) {
            captureServiceNodes.add(nodeName);
            captureServicePorts.add(nodePort);
            nodeLastUsedTime.put(nodeName, System.currentTimeMillis());
        }
    }

    private String generateUniqueNodeName() {
        int nodeCount = captureServiceNodes.size();
        String nodeName = "capture-service-node-" + nodeCount;

        while (captureServiceNodes.contains(nodeName)) {
            nodeCount++;
            nodeName = "capture-service-node-" + nodeCount;
        }

        return nodeName;
    }

    private boolean shouldScaleUp() {
        return captureServiceNodes.size() < MIN_NODES;
    }

    public void removeCaptureNode(String nodeName) {
        captureServiceNodes.remove(nodeName);
        captureServicePorts.remove(captureServiceNodes.indexOf(nodeName));
        dockerService.removeProcessorServiceNode(nodeName);
        nodeLastUsedTime.remove(nodeName);
    }

    private void sendMessageToNode(String nodeName, int nodePort, String message) {
        try {
            String url = "http://127.0.0.1:" + nodePort + "/capture/process";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(message, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Message successfully sent to node " + nodeName);
                nodeLastUsedTime.put(nodeName, System.currentTimeMillis());
            } else {
                System.out.println("Failed to send message to node " + nodeName + ": " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Failed to send message to node " + nodeName + ": " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void checkInactiveNodes() {
        long currentTime = System.currentTimeMillis();
        List<String> inactiveNodes = new ArrayList<>();

        for (String nodeName : captureServiceNodes) {
            if (isNodeInactive(nodeName, currentTime)) {
                inactiveNodes.add(nodeName);
            }
        }

        for (String nodeName : inactiveNodes) {
            removeCaptureNode(nodeName);
        }
    }

    private boolean isNodeInactive(String nodeName, long currentTime) {
        Long lastUsedTime = nodeLastUsedTime.get(nodeName);
        return lastUsedTime != null && (currentTime - lastUsedTime) > NODE_INACTIVITY_TIMEOUT;
    }
}
