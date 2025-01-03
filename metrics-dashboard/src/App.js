import React from "react";
import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import Dashboard from "./components/Dashboard";
import ProcessorMetrics from "./components/ProcessorMetrics";

function App() {
  return (
    <Router>
      <div>
        <Switch>
          <Route path="/" exact component={Dashboard} />
          <Route path="/processors" component={ProcessorMetrics} />
        </Switch>
      </div>
    </Router>
  );
}

export default App;
