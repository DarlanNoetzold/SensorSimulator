import React from "react";
import ReactDOM from "react-dom/client"; // Nova API do ReactDOM para versões >= 18
import App from "./App";

const root = ReactDOM.createRoot(document.getElementById("root")); // Criação do root
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
