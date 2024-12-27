from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import pandas as pd
from statsmodels.tsa.arima.model import ARIMA
import psycopg2

app = FastAPI()

class PredictionRequest(BaseModel):
    dataset_path: str
    p: int
    d: int
    q: int
    num_predictions: int
    interval: int

@app.post("/train")
def train_model(request: PredictionRequest):
    try:
        # Load dataset
        data = pd.read_csv(request.dataset_path)
        series = data['value']

        # Train ARIMA model
        model = ARIMA(series, order=(request.p, request.d, request.q))
        model_fit = model.fit()

        # Predict
        predictions = model_fit.forecast(steps=request.num_predictions)
        intervals = [request.interval * i for i in range(1, request.num_predictions + 1)]

        # Save predictions to PostgreSQL
        conn = psycopg2.connect("dbname='predictions' user='postgres' password='password' host='db'")
        cursor = conn.cursor()

        cursor.executemany(
            "INSERT INTO predictions (value, interval) VALUES (%s, %s)",
            zip(predictions, intervals),
        )
        conn.commit()
        conn.close()

        return {"status": "success", "predictions": predictions.tolist()}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
