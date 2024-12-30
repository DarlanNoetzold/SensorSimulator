import React from "react";
import ConfigForm from "./components/ConfigForm";
import SensorPredictionSearch from './components/SensorPredictionSearch';
import SendPredictions from './components/SendPredictions';

function App() {
  return (
    <div className="App">
      <h1>Simulador de Predições</h1>
      <ConfigForm />
      <h1>Sensor Predictions</h1>
      <SensorPredictionSearch />
      <h1>Send Predictions</h1>
      <SendPredictions />
    </div>
  );
}

export default App;
