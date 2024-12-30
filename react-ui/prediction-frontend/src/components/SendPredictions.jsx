import React, { useState } from 'react';
import axios from 'axios';

const SendPredictionsButton = () => {
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');

    const sendPredictions = async () => {
        setLoading(true);
        try {
            const response = await axios.post('http://localhost:8070/production/send');
            setMessage(response.data);  // Espera o "Predictions sent to the queue!" da resposta
        } catch (error) {
            setMessage('Error sending predictions');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <button onClick={sendPredictions} disabled={loading}>
                {loading ? 'Sending...' : 'Send Predictions'}
            </button>
            {message && <p>{message}</p>}
        </div>
    );
};

export default SendPredictionsButton;
