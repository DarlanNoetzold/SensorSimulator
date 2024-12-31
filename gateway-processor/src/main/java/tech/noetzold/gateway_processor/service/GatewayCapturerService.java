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

@Service
public class GatewayCapturerService {

    @Autowired
    private DockerService dockerService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper; // Usado para serializar o objeto Prediction em JSON

    private List<String> captureServiceNodes = new ArrayList<>();
    private List<Integer> captureServicePorts = new ArrayList<>(); // Para armazenar as portas
    private int currentNodeIndex = 0;

    private static final int MIN_NODES = 3;
    private static final int MAX_NODES = 10; // Limite máximo para evitar criação excessiva
    private static final long NODE_INACTIVITY_TIMEOUT = 5 * 60 * 1000; // 5 minutos em milissegundos

    @RabbitListener(queues = "sensorDataCaptured")
    public void handleMessage(String message) {
        if (captureServiceNodes.size() < MIN_NODES) {
            createNewNode();
        }

        // Balanceamento de carga (round-robin)
        String selectedNode = captureServiceNodes.get(currentNodeIndex);
        int selectedPort = captureServicePorts.get(currentNodeIndex);  // Pega a porta associada ao nó
        currentNodeIndex = (currentNodeIndex + 1) % captureServiceNodes.size();

        System.out.println("Sent message to Capture-Service node: " + selectedNode);

        // Criar um objeto Prediction com valores extraídos da message e enviar via HTTP
        sendMessageToNode(selectedNode, selectedPort, message);

        // Verificar se é necessário escalar mais nós
        if (captureServiceNodes.size() < MAX_NODES && shouldScaleUp()) {
            createNewNode();
        }
    }

    private void createNewNode() {
        String nodeName = generateUniqueNodeName();
        System.out.println("Creating new Capture-Service node: " + nodeName);

        // Gerar a porta externa para cada nó (9000 para o primeiro, 9001 para o segundo, etc.)
        int nodePort = 9000 + captureServiceNodes.size(); // Exemplo: 9001, 9002, 9003...

        String containerId = dockerService.createCaptureServiceNode(nodeName, nodePort);

        if (containerId != null) {
            captureServiceNodes.add(nodeName); // Adicionar o nó à lista de nós
            captureServicePorts.add(nodePort); // Adicionar a porta ao nó
            System.out.println("New Capture-Service node created: " + nodeName);
        } else {
            System.out.println("Failed to create Capture-Service node: " + nodeName);
        }
    }

    // Gera um nome único para o nó
    private String generateUniqueNodeName() {
        int nodeCount = captureServiceNodes.size();
        String nodeName = "capture-service-node-" + nodeCount;

        while (captureServiceNodes.contains(nodeName)) {
            nodeCount++;
            nodeName = "capture-service-node-" + nodeCount;
        }

        return nodeName;
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
    private void sendMessageToNode(String nodeName, int nodePort, String message) {
        try {
            // Construir a URL para o serviço Capture-Service
            String url = "http://127.0.0.1:" + nodePort + "/capture/process"; // Usando a porta do nó

            // Configurar o cabeçalho Content-Type para JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Criar a entidade com a mensagem e o cabeçalho
            HttpEntity<String> request = new HttpEntity<>(message, headers);

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

    // Método agendado para verificar a inatividade dos nós e removê-los se necessário
    @Scheduled(fixedRate = 60 * 1000) // Executa a cada minuto
    public void checkInactiveNodes() {
        long currentTime = System.currentTimeMillis();
        List<String> inactiveNodes = new ArrayList<>();

        for (String nodeName : captureServiceNodes) {
            // Verifique se o nó está inativo por mais de 5 minutos (ajuste conforme necessário)
            if (isNodeInactive(nodeName, currentTime)) {
                inactiveNodes.add(nodeName);
            }
        }

        // Remover os nós inativos
        for (String nodeName : inactiveNodes) {
            removeCaptureNode(nodeName);
        }
    }

    // Método para verificar se o nó está inativo
    private boolean isNodeInactive(String nodeName, long currentTime) {
        // Lógica de inatividade, você pode armazenar o tempo de última atividade em um mapa ou banco de dados
        // Aqui vamos simplesmente simular o tempo de inatividade
        return (currentTime - 0) > NODE_INACTIVITY_TIMEOUT;
    }
}
