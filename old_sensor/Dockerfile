FROM python:3.9-slim
RUN pip install paho-mqtt

# Copiar os scripts para o contêiner
COPY sensor_simulator.py /app/sensor_simulator.py
COPY wait-for-mosquitto.sh /app/wait-for-mosquitto.sh

# Dar permissão de execução ao script de espera
RUN chmod +x /app/wait-for-mosquitto.sh

# Comando de entrada para esperar pelo Mosquitto e iniciar o simulador
ENTRYPOINT ["python", "/app/sensor_simulator.py"]
