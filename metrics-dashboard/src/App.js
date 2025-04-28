import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Route, Routes, Link } from "react-router-dom";
import { Navbar, Nav, Container } from "react-bootstrap";
import { Line } from "react-chartjs-2";
import axios from "axios";
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend } from "chart.js";
import ProcessorMetrics from "./components/ProcessorMetrics"

import "bootstrap/dist/css/bootstrap.min.css";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const Dashboard = () => {
  const [sensorData, setSensorData] = useState([]);
  const [limit, setLimit] = useState(50);

  useEffect(() => {
    axios.get(`http://localhost:8780/api/sensors?limit=${limit}`)
      .then(response => {
        setSensorData(response.data);
      })
      .catch(error => console.log(error));
  }, [limit]);

  const groupedData = sensorData.reduce((acc, item) => {
    if (!acc[item.sensorName]) {
      acc[item.sensorName] = [];
    }
    acc[item.sensorName].push(item);
    return acc;
  }, {});

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
    <Container>
      <h1>Dashboard de Métricas dos Sensores</h1>
      
      {/* Input para ajustar o limite */}
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

      {/* Renderizando gráficos para cada sensorName */}
      {Object.keys(groupedData).map((sensorName) => (
        <div key={sensorName}>
          <h3>{`Sensor: ${sensorName}`}</h3>
          <Line data={renderChartData(sensorName, groupedData[sensorName])} />
        </div>
      ))}
    </Container>
  );
};

const App = () => {
  return (
    <Router>
      <Navbar bg="dark" variant="dark" expand="lg">
        <Container>
          <Navbar.Brand as={Link} to="/">Dashboard</Navbar.Brand>
          <Navbar.Toggle aria-controls="navbar-nav" />
          <Navbar.Collapse id="navbar-nav">
            <Nav className="ml-auto">
              <Nav.Link as={Link} to="/">Dashboard</Nav.Link>
              <Nav.Link as={Link} to="/metrics">Metrics</Nav.Link>
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      <Container className="mt-4">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/metrics" element={<ProcessorMetrics />} />
        </Routes>
      </Container>
    </Router>
  );
};

export default App;
