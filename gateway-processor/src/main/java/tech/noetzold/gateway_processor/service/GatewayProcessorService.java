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
    private static final long NODE_INACTIVITY_TIMEOUT = 5 * 60 * 1000;

    private Map<String, Long> nodeLastUsedTime = new ConcurrentHashMap<>();

    @RabbitListener(queues = "sensorDataCaptured")
    public void handleMessage(String message) {
        if (processorServiceNodes.size() < MIN_NODES) {
            createNewNode();
        }

        String selectedNode = processorServiceNodes.get(currentNodeIndex);
        int selectedPort = processorServicePorts.get(currentNodeIndex);
        currentNodeIndex = (currentNodeIndex + 1) % processorServiceNodes.size();

        sendMessageToNode(selectedNode, selectedPort, message);

        if (processorServiceNodes.size() < MAX_NODES && shouldScaleUp()) {
            createNewNode();
        }
    }

    private void createNewNode() {
        String nodeName = generateUniqueNodeName();

        // Gerar a porta externa para cada nó (9000 para o primeiro, 9001 para o segundo, etc.)
        int nodePort = 9000 + processorServiceNodes.size();
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
        processorServiceNodes.remove(nodeName);
        processorServicePorts.remove(processorServiceNodes.indexOf(nodeName));
        dockerService.removeProcessorServiceNode(nodeName);
        nodeLastUsedTime.remove(nodeName);
    }

    private void sendMessageToNode(String nodeName, int nodePort, String message) {
        try {
            String url = "http://127.0.0.1:" + nodePort + "/processor/process";  // Alterado para /processor/process
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
