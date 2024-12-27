from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import pandas as pd
from statsmodels.tsa.arima.model import ARIMA
import psycopg2
from datetime import datetime, timedelta

app = FastAPI()


class PredictionRequest(BaseModel):
    dataset_path: str
    p: int
    d: int
    q: int
    num_predictions: int
    interval: int
    sensor_name: str
    unit: str


@app.post("/train")
def train_model(request: PredictionRequest):
    try:
        # Load dataset
        data = pd.read_csv(request.dataset_path)
        if 'value' not in data.columns:
            raise HTTPException(status_code=400, detail="Dataset must contain a 'value' column.")

        series = data['value']

        # Train ARIMA model
        model = ARIMA(series, order=(request.p, request.d, request.q))
        model_fit = model.fit()

        # Predict
        predictions = model_fit.forecast(steps=request.num_predictions)
        current_date = datetime.now()
        predicted_dates = [current_date + timedelta(seconds=request.interval * i) for i in
                           range(1, request.num_predictions + 1)]

        # Save predictions to PostgreSQL
        conn = psycopg2.connect("dbname='predictions' user='postgres' password='password' host='db'")
        cursor = conn.cursor()

        # Create predictions table if not exists
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS predictions (
                id SERIAL PRIMARY KEY,
                sensor_name VARCHAR(255),
                unit VARCHAR(50),
                interval INTEGER,
                predicted_date TIMESTAMP,
                value DOUBLE PRECISION
            )
        """)

        # Insert predictions
        prediction_records = [
            (request.sensor_name, request.unit, request.interval, predicted_dates[i], predictions[i])
            for i in range(request.num_predictions)
        ]

        cursor.executemany(
            "INSERT INTO predictions (sensor_name, unit, interval, predicted_date, value) VALUES (%s, %s, %s, %s, %s)",
            prediction_records,
        )

        conn.commit()

        # Optional: Calculate and save statistics
        mean = predictions.mean()
        std_dev = predictions.std()
        variance = predictions.var()

        cursor.execute("""
            CREATE TABLE IF NOT EXISTS prediction_statistics (
                id SERIAL PRIMARY KEY,
                prediction_id INTEGER REFERENCES predictions(id),
                mean DOUBLE PRECISION,
                standard_deviation DOUBLE PRECISION,
                variance DOUBLE PRECISION
            )
        """)

        # Save statistics for the prediction batch
        cursor.execute("""
            INSERT INTO prediction_statistics (mean, standard_deviation, variance)
            VALUES (%s, %s, %s)
        """, (mean, std_dev, variance))

        conn.commit()
        conn.close()

        return {
            "status": "success",
            "predictions": [
                {"date": str(predicted_dates[i]), "value": predictions[i]}
                for i in range(request.num_predictions)
            ],
            "statistics": {
                "mean": mean,
                "standard_deviation": std_dev,
                "variance": variance
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
