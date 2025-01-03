import React, { useEffect, useState } from "react";
import axios from "axios";
import { Line } from "react-chartjs-2";
import { Tab, Tabs } from "react-bootstrap";
import { Chart as ChartJS, CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend } from "chart.js";

ChartJS.register(CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend);

const ProcessorMetrics = () => {
  const [processorMetrics, setProcessorMetrics] = useState({});
  const [activeTab, setActiveTab] = useState("");

  useEffect(() => {
    axios
      .get("http://localhost:8780/api/metrics/latest?limit=10")
      .then((response) => {
        const groupedByProcessor = response.data.reduce((acc, metric) => {
          if (!acc[metric.processorId]) {
            acc[metric.processorId] = [];
          }
          acc[metric.processorId].push(metric);
          return acc;
        }, {});
        setProcessorMetrics(groupedByProcessor);
        setActiveTab(Object.keys(groupedByProcessor)[0]); // Set default tab
      })
      .catch((error) => {
        console.error("Error fetching processor metrics", error);
      });
  }, []);

  const getChartData = (processorId, metricName) => {
    const processorData = processorMetrics[processorId] || [];
    const labels = processorData.map((metric) => metric.predictedDate);
    const values = processorData.map((metric) => metric[metricName]);
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

  return (
    <div>
      <h1>Processor Metrics Dashboard</h1>
      <Tabs activeKey={activeTab} onSelect={(k) => setActiveTab(k)} id="processor-tabs">
        {Object.keys(processorMetrics).map((processorId) => (
          <Tab eventKey={processorId} title={processorId} key={processorId}>
            <div>
              <h3>Memory Usage</h3>
              <Line data={getChartData(processorId, "memoryUsage")} />
            </div>
            <div>
              <h3>CPU Usage</h3>
              <Line data={getChartData(processorId, "cpuUsage")} />
            </div>
          </Tab>
        ))}
      </Tabs>
    </div>
  );
};

export default ProcessorMetrics;
