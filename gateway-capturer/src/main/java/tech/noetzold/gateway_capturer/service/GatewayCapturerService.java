package tech.noetzold.gateway_capturer.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class GatewayCapturerService {

    @Autowired
    private DockerService dockerService;

    @Autowired
    private RestTemplate restTemplate;

    private List<String> captureServiceNodes = new ArrayList<>();
    private int currentNodeIndex = 0;

    private static final int MIN_NODES = 3;
    private static final int MAX_NODES = 10; // Limite máximo para evitar criação excessiva

    @RabbitListener(queues = "productionQueue")
    public void handleMessage(String message) {
        if (captureServiceNodes.size() < MIN_NODES) {
            createNewNode();
        }

        // Balanceamento de carga (round-robin)
        String selectedNode = captureServiceNodes.get(currentNodeIndex);
        currentNodeIndex = (currentNodeIndex + 1) % captureServiceNodes.size();

        System.out.println("Sent message to Capture-Service node: " + selectedNode);

        // Enviar a mensagem para o nó selecionado via HTTP
        sendMessageToNode(selectedNode, message);

        if (captureServiceNodes.size() < MAX_NODES && shouldScaleUp()) {
            createNewNode();
        }
    }

    private void createNewNode() {
        String nodeName = "capture-service-node-" + captureServiceNodes.size();
        System.out.println("Creating new Capture-Service node: " + nodeName);

        String containerId = dockerService.createCaptureServiceNode(nodeName);

        if (containerId != null) {
            captureServiceNodes.add(nodeName); // Adicionar o nó à lista de nós
            System.out.println("New Capture-Service node created: " + nodeName);
        } else {
            System.out.println("Failed to create Capture-Service node: " + nodeName);
        }
    }

    // Método para verificar se é necessário criar mais nós (exemplo: baseado em alguma métrica)
    private boolean shouldScaleUp() {
        return captureServiceNodes.size() < MIN_NODES;
    }

    // Método para remover um nó Capture-Service (caso seja necessário)
    public void removeCaptureNode(String nodeName) {
        captureServiceNodes.remove(nodeName);
        dockerService.removeCaptureServiceNode(nodeName);
    }

    // Método para enviar a mensagem ao nó Capture-Service via HTTP
    private void sendMessageToNode(String nodeName, String message) {
        try {
            // Construir a URL para o serviço Capture-Service
            String url = "http://" + nodeName + ":8081/capture/process"; // Endereço HTTP do Capture-Service (ajuste conforme necessário)

            // Criar a entidade com a mensagem
            HttpEntity<String> request = new HttpEntity<>(message);

            // Enviar a mensagem para o endpoint /process
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            // Verificar a resposta
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Message successfully sent to node " + nodeName);
            } else {
                System.out.println("Failed to send message to node " + nodeName + ": " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Failed to send message to node " + nodeName + ": " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60 * 1000) // Executa a cada minuto
    public void checkInactiveNodes() {
        dockerService.cleanupInactiveNodes();
    }
}
