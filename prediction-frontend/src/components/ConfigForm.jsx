import React, { useState } from "react";
import axios from "axios";
import FileUploader from "./FileUploader";

const ConfigForm = () => {
  const [file, setFile] = useState(null);
  const [formData, setFormData] = useState({
    sensorName: "",
    unit: "",
    p: 0,
    d: 0,
    q: 0,
    numPredictions: 1,
    interval: 60,
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!file) {
      alert("Por favor, selecione um arquivo!");
      return;
    }

    const data = new FormData();
    data.append("sensorName", formData.sensorName);
    data.append("unit", formData.unit);
    data.append("p", formData.p);
    data.append("d", formData.d);
    data.append("q", formData.q);
    data.append("numPredictions", formData.numPredictions);
    data.append("interval", formData.interval);
    data.append("file", file);

    try {
      const response = await axios.post("http://localhost:8060/config-service/upload", data, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      alert("Configuração enviada com sucesso!");
      console.log(response.data);
    } catch (error) {
      console.error(error);
      alert("Erro ao enviar a configuração!");
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Configuração de Predições</h2>
      <div>
        <label>Nome do Sensor:</label>
        <input
          type="text"
          name="sensorName"
          value={formData.sensorName}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Unidade:</label>
        <input
          type="text"
          name="unit"
          value={formData.unit}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Parâmetro P:</label>
        <input
          type="number"
          name="p"
          value={formData.p}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Parâmetro D:</label>
        <input
          type="number"
          name="d"
          value={formData.d}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Parâmetro Q:</label>
        <input
          type="number"
          name="q"
          value={formData.q}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Número de Predições:</label>
        <input
          type="number"
          name="numPredictions"
          value={formData.numPredictions}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Intervalo (em segundos):</label>
        <input
          type="number"
          name="interval"
          value={formData.interval}
          onChange={handleChange}
          required
        />
      </div>
      <FileUploader onFileSelect={(selectedFile) => setFile(selectedFile)} />
      <button type="submit">Enviar Configuração</button>
    </form>
  );
};

export default ConfigForm;
