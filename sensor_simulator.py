import paho.mqtt.client as mqtt
import time
import random
import threading

broker = "mosquitto"
port = 1883
username = "user"
password = "user"

client = mqtt.Client()
client.username_pw_set(username, password)
client.connect(broker, port, 60)

def generate_humidity():
    return round(random.uniform(30.0, 70.0), 2)

def generate_temperature():
    return round(random.uniform(15.0, 35.0), 2)

def generate_presence():
    return random.choice([0, 1])

def generate_motion():
    return random.choice([0, 1])

def generate_co2():
    return round(random.uniform(300.0, 1000.0), 2)

def generate_energy():
    return round(random.uniform(0.0, 5.0), 2)

def generate_water_flow():
    return round(random.uniform(0.0, 10.0), 2)

def publish_sensor_data(sensor_type, generate_function, interval):
    while True:
        value = generate_function()
        topic = f"zigbee2mqtt/{sensor_type}"
        client.publish(topic, value)
        print(f"{sensor_type.capitalize()}: {value}")
        time.sleep(interval)

sensors = {
    "humidity": generate_humidity,
    "temperature": generate_temperature,
    "presence": generate_presence,
    "motion": generate_motion,
    "co2": generate_co2,
    "energy": generate_energy,
    "water_flow": generate_water_flow,
}

for sensor_type, generate_function in sensors.items():
    threading.Thread(target=publish_sensor_data, args=(sensor_type, generate_function, 50)).start()

client.loop_forever()
