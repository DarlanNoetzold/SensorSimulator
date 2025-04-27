package tech.noetzold.gateway_capturer.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

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


    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 10000; 

    @RabbitListener(queues = "productionQueue")
    public void handleMessage(String message) {
        if (captureServiceNodes.size() < MIN_NODES) {
            createNewNode();
        }

        String selectedNode = captureServiceNodes.get(currentNodeIndex);
        int selectedPort = captureServicePorts.get(currentNodeIndex);
        currentNodeIndex = (currentNodeIndex + 1) % captureServiceNodes.size();

        System.out.println("Sent message to Capture-Service node: " + selectedNode);

        sendMessageToNode(selectedNode, selectedPort, message);

        if (captureServiceNodes.size() < MAX_NODES && shouldScaleUp()) {
            createNewNode();
        }
    }

    private void createNewNode() {
        String nodeName = generateUniqueNodeName();
        System.out.println("Creating new Capture-Service node: " + nodeName);

        int nodePort = 9000 + captureServiceNodes.size();

        String containerId = dockerService.createCaptureServiceNode(nodeName, nodePort);

        if (containerId != null) {
            captureServiceNodes.add(nodeName);
            captureServicePorts.add(nodePort);
            nodeLastUsedTime.put(nodeName, System.currentTimeMillis());
            System.out.println("New Capture-Service node created: " + nodeName);
        } else {
            System.out.println("Container with name " + nodeName + " already exists. Sending message to this node.");
            sendMessageToNode(nodeName, nodePort, "Message for existing node: " + nodeName);
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
        dockerService.removeCaptureServiceNode(nodeName);
        nodeLastUsedTime.remove(nodeName);

    }

    private void sendMessageToNode(String nodeName, int nodePort, String message) {
        int retries = 0;

        while (retries < MAX_RETRIES) {
            try {
                String url = "http://127.0.0.1:" + nodePort + "/capture/process"; // Usando a porta do nó

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> request = new HttpEntity<>(message, headers);

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
                    Thread.sleep(RETRY_DELAY_MS); // Espera entre tentativas
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
