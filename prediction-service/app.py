from flask import Flask, request, jsonify
import pandas as pd
from statsmodels.tsa.arima.model import ARIMA
import psycopg2
from datetime import datetime, timedelta
import io

app = Flask(__name__)

@app.route('/train', methods=['POST'])
def train_model():
    try:
        # Obter parâmetros do formulário
        sensor_name = request.form['sensor_name']
        unit = request.form['unit']
        p = int(request.form['p'])
        d = int(request.form['d'])
        q = int(request.form['q'])
        num_predictions = int(request.form['num_predictions'])
        interval = int(request.form['interval'])
        file = request.files['file']

        # Validar formato do arquivo
        if not file.filename.endswith('.csv'):
            return jsonify({"error": "Only CSV files are currently supported."}), 400

        # Ler o conteúdo do arquivo
        content = file.read().decode('utf-8')
        data = pd.read_csv(io.StringIO(content))

        if 'value' not in data.columns:
            return jsonify({"error": "Dataset must contain a 'value' column."}), 400

        series = data['value']

        # Treinar o modelo ARIMA
        model = ARIMA(series, order=(p, d, q))
        model_fit = model.fit()

        # Prever valores
        predictions = model_fit.forecast(steps=num_predictions)
        current_date = datetime.now()
        predicted_dates = [current_date + timedelta(seconds=interval * i) for i in range(1, num_predictions + 1)]

        # Conectar ao PostgreSQL
        conn = psycopg2.connect("dbname='predictions' user='postgres' password='postgres' host='localhost'")
        cursor = conn.cursor()

        # Criar tabelas, se não existirem
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

        cursor.execute("""
            CREATE TABLE IF NOT EXISTS prediction_statistics (
                id SERIAL PRIMARY KEY,
                mean DOUBLE PRECISION,
                standard_deviation DOUBLE PRECISION,
                variance DOUBLE PRECISION
            )
        """)

        # Gerar predições
        predictions = model_fit.forecast(steps=num_predictions)
        current_date = datetime.now()
        predicted_dates = [current_date + timedelta(seconds=interval * i) for i in range(1, num_predictions + 1)]

        # Log de depuração
        print(f"Predictions: {predictions}")
        print(f"Predicted Dates: {predicted_dates}")
        print(f"Number of predictions: {len(predictions)}")
        print(f"Number of predicted dates: {len(predicted_dates)}")

        # Verificar tamanhos consistentes
        if len(predictions) != len(predicted_dates):
            raise ValueError("Mismatch between number of predictions and predicted dates.")

        # Ajustar os registros para inserção
        prediction_records = [
            (
                str(sensor_name),
                str(unit),
                int(interval),
                predicted_dates[i].strftime('%Y-%m-%d %H:%M:%S'),
                float(predictions[i])
            )
            for i in range(len(predictions))
        ]

        # Log dos registros
        print(f"Prediction Records: {prediction_records}")

        # Inserir no banco de dados
        try:
            cursor.executemany(
                "INSERT INTO predictions (sensor_name, unit, interval, predicted_date, value) VALUES (%s, %s, %s, %s, %s)",
                prediction_records
            )
            conn.commit()
        except Exception as e:
            print(f"Error inserting predictions: {e}")
            raise

        # Calcular estatísticas e salvar no banco
        mean = predictions.mean()
        std_dev = predictions.std()
        variance = predictions.var()

        cursor.execute("""
            INSERT INTO prediction_statistics (mean, standard_deviation, variance)
            VALUES (%s, %s, %s)
        """, (mean, std_dev, variance))

        conn.commit()
        conn.close()

        # Retornar resultado
        return jsonify({
            "status": "success",
            "predictions": [
                {"date": str(predicted_dates[i]), "value": predictions[i]}
                for i in range(num_predictions)
            ],
            "statistics": {
                "mean": mean,
                "standard_deviation": std_dev,
                "variance": variance
            }
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, debug=False)
