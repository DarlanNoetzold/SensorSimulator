import React, { useEffect, useState } from "react";
import axios from "axios";
import { Line } from "react-chartjs-2";
import { Tab, Tabs, Form } from "react-bootstrap";
import { Chart as ChartJS, CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend } from "chart.js";

ChartJS.register(CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend);

const ProcessorMetrics = () => {
  const [processorMetrics, setProcessorMetrics] = useState({});
  const [activeTab, setActiveTab] = useState("");
  const [limit, setLimit] = useState(10); // Default limit is set to 10

  // Fetch the metrics based on the limit value
  useEffect(() => {
    axios
      .get(`http://localhost:8780/api/metrics/latest?limit=${limit}`)
      .then((response) => {
        const groupedByProcessor = response.data.reduce((acc, metric) => {
          if (!acc[metric.processorId]) {
            acc[metric.processorId] = [];
          }
          acc[metric.processorId].push(metric);
          return acc;
        }, {});
        setProcessorMetrics(groupedByProcessor);
        setActiveTab(Object.keys(groupedByProcessor)[0]); // Set default tab to the first processorId
      })
      .catch((error) => {
        console.error("Error fetching processor metrics", error);
      });
  }, [limit]);

  // Function to generate chart data for any given metric
  const getChartData = (processorId, metricName) => {
    const processorData = processorMetrics[processorId] || []; // Verifica se os dados do processador existem
    
    // Converter predictedDate para string
    const labels = processorData.map((metric) => metric.predictedDate ? metric.predictedDate : ""); // Convert LocalDateTime to string
    const values = processorData.map((metric) => metric[metricName]);

    // Logar os dados para verificar se a estrutura está correta
    console.log(`Data for ${metricName}:`, values);

    // Se os valores não estiverem definidos, não renderiza o gráfico
    if (values.some(value => value === undefined)) {
      console.error(`Erro: algum valor de ${metricName} está undefined.`);
      return {}; // Retorna um objeto vazio se algum valor for undefined
    }

    return {
      labels,
      datasets: [
        {
          label: metricName,
          data: values,
          fill: false,
          borderColor: "rgb(75, 192, 192)",
          tension: 0.1,
        },
      ],
    };
  };

  // Function to render variance map as individual charts
  const renderVarianceCharts = (processorId) => {
    const processorData = processorMetrics[processorId] || [];
    const varianceData = processorData.map((metric) => metric.varianceMap || {});

    // Extract all the variance keys dynamically
    const varianceKeys = [...new Set(varianceData.flatMap((item) => Object.keys(item)))];

    return varianceKeys.map((key) => (
      <div key={key}>
        <h3>{`Variance: ${key}`}</h3>
        <Line
          data={{
            labels: processorData.map((metric) => metric.predictedDate),
            datasets: [
              {
                label: key,
                data: processorData.map((metric) => metric.varianceMap[key]),
                fill: false,
                borderColor: "rgba(255, 159, 64, 1)",
                tension: 0.1,
              },
            ],
          }}
        />
      </div>
    ));
  };

  // Handle limit change
  const handleLimitChange = (event) => {
    const newLimit = Number(event.target.value);
    if (newLimit > 0) {
      setLimit(newLimit); // Set the limit based on user input
    }
  };

  return (
    <div>
      <h1>Processor Metrics Dashboard</h1>

      {/* Input to allow the user to type the number of records (limit) */}
      <div>
        <label>Limit: </label>
        <Form.Control
          type="number"
          value={limit}
          onChange={handleLimitChange}
          style={{ width: "100px", marginBottom: "20px" }}
        />
      </div>

      <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k)} id="processor-tabs">
        {Object.keys(processorMetrics).map((processorId) => (
          <Tab eventKey={processorId} title={processorId} key={processorId}>
            <div>
              <h3>CPU Usage</h3>
              <Line data={getChartData(processorId, "cpuUsage")} />
            </div>
            <div>
              <h3>Memory Usage</h3>
              <Line data={getChartData(processorId, "memoryUsage")} />
            </div>
            <div>
              <h3>Thread Count</h3>
              <Line data={getChartData(processorId, "threadCount")} />
            </div>
            <div>
              <h3>Total Data Received</h3>
              <Line data={getChartData(processorId, "totalDataReceived")} />
            </div>
            <div>
              <h3>Total Data Filtered</h3>
              <Line data={getChartData(processorId, "totalDataFiltered")} />
            </div>
            <div>
              <h3>Total Data Compressed</h3>
              <Line data={getChartData(processorId, "totalDataCompressed")} />
            </div>
            <div>
              <h3>Total Data Aggregated</h3>
              <Line data={getChartData(processorId, "totalDataAggregated")} />
            </div>
            <div>
              <h3>Total Data After Heuristics</h3>
              <Line data={getChartData(processorId, "totalDataAfterHeuristics")} />
            </div>
            <div>
              <h3>Error Count</h3>
              <Line data={getChartData(processorId, "errorCount")} />
            </div>

            {/* Render variance maps dynamically for each processor */}
            {renderVarianceCharts(processorId)}
          </Tab>
        ))}
      </Tabs>
    </div>
  );
};

export default ProcessorMetrics;
