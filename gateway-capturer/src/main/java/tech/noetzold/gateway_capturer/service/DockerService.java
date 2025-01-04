package tech.noetzold.gateway_capturer.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DockerService {

    private final DockerClient dockerClient;
    private final Map<String, Long> nodeLastUsedTime = new HashMap<>();
    private static final long TIMEOUT = 5 * 60 * 1000; // 5 minutos em milissegundos

    public DockerService() {
        // Conectando-se ao Docker daemon (assumindo que o Docker está rodando localmente)
        this.dockerClient = DockerClientBuilder
                .getInstance("tcp://localhost:2375")
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
    }

    /**
     * Cria um novo contêiner do Capture-Service se o nome ainda não estiver em uso.
     * @param nodeName Nome do nó Capture-Service.
     * @param nodePort Porta a ser exposta para o contêiner.
     * @return O ID do container criado ou null se o nome já estiver em uso.
     */
    public String createCaptureServiceNode(String nodeName, int nodePort) {
        try {
            // Verifica se o contêiner já existe
            boolean containerExists = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .exec().stream()
                    .anyMatch(container -> container.getNames()[0].equals("/" + nodeName));

            if (containerExists) {
                System.out.println("Container with name " + nodeName + " already exists.");
                return null;
            }

            Ports portBindings = new Ports();

            // Criar o contêiner Capture-Service e expor a porta dinâmica
            ExposedPort exposedPort = new ExposedPort(9000);
            portBindings.bind(exposedPort, Ports.Binding.bindPort(nodePort));

            CreateContainerResponse container = dockerClient.createContainerCmd("tech/noetzold/capture-service:latest")
                    .withName(nodeName)
                    .withExposedPorts(exposedPort)  // Expondo as portas do RabbitMQ
                    .withPortBindings(portBindings)  // Mapeia as portas do contêiner para o host
                    .withEnv("SENSOR_PROCESSOR_ENV=" + nodeName)  // Define a variável sensor.processor.id
                    .exec();

            // Subir o contêiner
            dockerClient.startContainerCmd(container.getId()).exec();
            nodeLastUsedTime.put(nodeName, System.currentTimeMillis()); // Marca a última vez que o nó foi usado

            return container.getId(); // Retorna o ID do container criado
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Remove um contêiner do Capture-Service.
     * @param containerId O ID do contêiner a ser removido.
     */
    public void removeCaptureServiceNode(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
            System.out.println("Container " + containerId + " removed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica e remove os contêineres que não foram usados nos últimos 5 minutos.
     */
    public void cleanupInactiveNodes() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : nodeLastUsedTime.entrySet()) {
            if (currentTime - entry.getValue() > TIMEOUT) {
                String nodeName = entry.getKey();
                System.out.println("Removing inactive node: " + nodeName);
                // Remove o nó
                removeCaptureServiceNode(nodeName);
                nodeLastUsedTime.remove(nodeName);
            }
        }
    }

    /**
     * Atualiza o tempo de uso de um nó.
     * @param nodeName O nome do nó.
     */
    public void updateNodeUsageTime(String nodeName) {
        nodeLastUsedTime.put(nodeName, System.currentTimeMillis());
    }
}
