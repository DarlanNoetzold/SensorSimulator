import React, { useState } from 'react';

const SensorPredictionSearch = () => {
    const [sensorName, setSensorName] = useState('');
    const [predictions, setPredictions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleSearch = async () => {
        if (!sensorName) {
            alert('Please enter a sensor name');
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`http://localhost:8090/data-handler/predictions/${sensorName}`);
            
            if (!response.ok) {
                throw new Error('Failed to fetch predictions');
            }

            const data = await response.json();
            setPredictions(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="sensor-prediction-search">
            <h2>Search Sensor Predictions</h2>
            <div>
                <input
                    type="text"
                    value={sensorName}
                    onChange={(e) => setSensorName(e.target.value)}
                    placeholder="Enter Sensor Name"
                />
                <button onClick={handleSearch} disabled={loading}>
                    {loading ? 'Loading...' : 'Search'}
                </button>
            </div>

            {error && <p style={{ color: 'red' }}>{error}</p>}

            {predictions.length > 0 && (
                <div>
                    <h3>Predictions for "{sensorName}"</h3>
                    <table>
                        <thead>
                            <tr>
                                <th>Predicted Date</th>
                                <th>Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            {predictions.map((prediction, index) => (
                                <tr key={index}>
                                    <td>{new Date(prediction.predictedDate).toLocaleString()}</td>
                                    <td>{prediction.value}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default SensorPredictionSearch;
