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
      alert("Please select a file!");
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
      alert("Configuration successfully sent!");
      console.log(response.data);
    } catch (error) {
      console.error(error);
      alert("Error sending configuration!");
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Prediction Configuration</h2>
      <div>
        <label>Sensor Name:</label>
        <input
          type="text"
          name="sensorName"
          value={formData.sensorName}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Unit:</label>
        <input
          type="text"
          name="unit"
          value={formData.unit}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Parameter P:</label>
        <input
          type="number"
          name="p"
          value={formData.p}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Parameter D:</label>
        <input
          type="number"
          name="d"
          value={formData.d}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Parameter Q:</label>
        <input
          type="number"
          name="q"
          value={formData.q}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Number of Predictions:</label>
        <input
          type="number"
          name="numPredictions"
          value={formData.numPredictions}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Interval (in seconds):</label>
        <input
          type="number"
          name="interval"
          value={formData.interval}
          onChange={handleChange}
          required
        />
      </div>
      <FileUploader onFileSelect={(selectedFile) => setFile(selectedFile)} />
      <button type="submit">Submit Configuration</button>
    </form>
  );
};

export default ConfigForm;
