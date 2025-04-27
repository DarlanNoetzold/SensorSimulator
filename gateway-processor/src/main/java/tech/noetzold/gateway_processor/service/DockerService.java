package tech.noetzold.gateway_processor.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DockerService {

    private final DockerClient dockerClient;
    private final Map<String, Long> nodeLastUsedTime = new HashMap<>();
    private static final long TIMEOUT = 5 * 60 * 1000;

    public DockerService() {
        this.dockerClient = DockerClientBuilder
                .getInstance("tcp://localhost:2375")
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
    }

    /**
     * Cria um novo contêiner do Processor-Service se o nome ainda não estiver em uso.
     * @param nodeName Nome do nó Processor-Service.
     * @param nodePort Porta a ser exposta para o contêiner.
     * @return O ID do container criado ou null se o nome já estiver em uso.
     */
    public String createProcessorServiceNode(String nodeName, int nodePort) {
        try {
            boolean containerExists = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .exec().stream()
                    .anyMatch(container -> container.getNames()[0].equals("/" + nodeName));

            if (containerExists) {
                System.out.println("Container with name " + nodeName + " already exists.");
                return null;
            }

            Ports portBindings = new Ports();

            ExposedPort exposedPort = new ExposedPort(10000);
            portBindings.bind(exposedPort, Ports.Binding.bindPort(nodePort));

            CreateContainerResponse container = dockerClient.createContainerCmd("tech/noetzold/processor-service:latest")
                    .withName(nodeName)
                    .withExposedPorts(exposedPort)
                    .withPortBindings(portBindings)
                    .withEnv("sensor.processor.id=" + nodeName)
                    .exec();

            dockerClient.startContainerCmd(container.getId()).exec();
            nodeLastUsedTime.put(nodeName, System.currentTimeMillis());

            return container.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeProcessorServiceNode(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
            System.out.println("Container " + containerId + " removed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cleanupInactiveNodes() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : nodeLastUsedTime.entrySet()) {
            if (currentTime - entry.getValue() > TIMEOUT) {
                String nodeName = entry.getKey();
                System.out.println("Removing inactive node: " + nodeName);
                removeProcessorServiceNode(nodeName);
                nodeLastUsedTime.remove(nodeName);
            }
        }
    }

    public void updateNodeUsageTime(String nodeName) {
        nodeLastUsedTime.put(nodeName, System.currentTimeMillis());
    }
}
