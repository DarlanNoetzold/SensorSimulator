package tech.noetzold.gateway_capturer.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DockerService {

    private final DockerClient dockerClient;

    public DockerService() {
        // Conectando-se ao Docker daemon (assumindo que o Docker está rodando localmente)
        this.dockerClient = DockerClientBuilder
                .getInstance("tcp://localhost:2375")
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
    }

    /**
     * Cria um novo contêiner do Capture-Service.
     * @param nodeName Nome do nó Capture-Service.
     * @return O ID do container criado.
     */
    public String createCaptureServiceNode(String nodeName) {
        try {
            CreateContainerResponse container = dockerClient.createContainerCmd("tech/noetzold/capture-service:latest")
                    .withName(nodeName)
                    .withExposedPorts(new ExposedPort(8081))  // Expor a porta 8081 (ajuste conforme sua aplicação)
                    .exec();

            // Subir o contêiner
            dockerClient.startContainerCmd(container.getId()).exec();

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
