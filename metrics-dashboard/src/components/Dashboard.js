import React, { useEffect, useState } from "react";
import axios from "axios";
import { Line } from "react-chartjs-2";
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend } from "chart.js";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const Dashboard = () => {
  const [sensorData, setSensorData] = useState([]);
  const [limit, setLimit] = useState(50);  // Default limit set to 50

  useEffect(() => {
    // Fetching data with dynamic limit
    axios.get(`http://localhost:8780/api/sensors?limit=${limit}`)
      .then(response => {
        setSensorData(response.data);
      })
      .catch(error => console.log(error));
  }, [limit]);  // Re-fetch data when the limit changes
  
  // Agrupar os dados por sensorName
  const groupedData = sensorData.reduce((acc, item) => {
    if (!acc[item.sensorName]) {
      acc[item.sensorName] = [];
    }
    acc[item.sensorName].push(item);
    return acc;
  }, {});

  // Função para criar o gráfico para cada sensorName
  const renderChartData = (sensorName, data) => {
    return {
      labels: data.map((item) => item.predictedDate),
      datasets: [
        {
          label: `Valor do Sensor: ${sensorName}`,
          data: data.map((item) => item.value),
          borderColor: "rgba(75,192,192,1)",
          fill: false,
        },
      ],
    };
  };

  return (
    <div>
      <h1>Dashboard de Métricas dos Sensores</h1>

      {/* Adicionando input para ajustar o limite */}
      <div>
        <label htmlFor="limit">Número de registros a exibir: </label>
        <input
          id="limit"
          type="number"
          value={limit}
          onChange={(e) => setLimit(e.target.value)}
          min="1"
        />
      </div>

      {/* Renderizando um gráfico para cada sensorName */}
      {Object.keys(groupedData).map((sensorName) => (
        <div key={sensorName}>
          <h3>{`Sensor: ${sensorName}`}</h3>
          <Line data={renderChartData(sensorName, groupedData[sensorName])} />
        </div>
      ))}
    </div>
  );
};

export default Dashboard;
