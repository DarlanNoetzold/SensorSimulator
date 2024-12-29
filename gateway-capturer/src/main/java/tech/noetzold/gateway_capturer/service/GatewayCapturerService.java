package tech.noetzold.gateway_capturer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GatewayCapturerService {

    @Autowired
    private DockerService dockerService;

    private List<String> captureServiceNodes = new ArrayList<>();
    private int currentNodeIndex = 0;

    @RabbitListener(queues = "productionQueue")
    public void handleMessage(String message) {
        // Se não há nós suficientes ou a carga é alta, cria mais nós
        if (captureServiceNodes.size() < 3) { // Exemplo: Subir mais nós se houver menos de 3
            createNewNode();
        }

        // Balanceamento de carga (round-robin)
        String selectedNode = captureServiceNodes.get(currentNodeIndex);
        currentNodeIndex = (currentNodeIndex + 1) % captureServiceNodes.size();

        // Enviar a mensagem para o nó selecionado
        // (Aqui, você pode implementar a lógica de envio para o nó correto)
        System.out.println("Sent message to Capture-Service node: " + selectedNode);
    }

    private void createNewNode() {
        String nodeName = "capture-service-node-" + captureServiceNodes.size();
        System.out.println("Creating new Capture-Service node: " + nodeName);

        // Criar um novo contêiner do Capture-Service usando o Docker
        String containerId = dockerService.createCaptureServiceNode(nodeName);

        if (containerId != null) {
            captureServiceNodes.add(nodeName); // Adicionar o nó à lista de nós
            System.out.println("New Capture-Service node created: " + nodeName);
        }
    }

    // Método para remover um nó Capture-Service (caso seja necessário)
    public void removeCaptureNode(String nodeName) {
        captureServiceNodes.remove(nodeName);
        dockerService.removeCaptureServiceNode(nodeName);
    }
}

