#!/bin/sh

# Host e porta do Mosquitto
HOST="mosquitto"
PORT=1883

# Função para verificar se o Mosquitto está disponível
is_mosquitto_up() {
  nc -z $HOST $PORT
  return $?
}

# Esperar até que o Mosquitto esteja disponível
echo "Waiting for Mosquitto to be available at $HOST:$PORT..."
while ! is_mosquitto_up; do
  sleep 1
done

echo "Mosquitto is available, starting sensor simulator..."

# Iniciar o simulador de sensores
exec python /app/sensor_simulator.py
