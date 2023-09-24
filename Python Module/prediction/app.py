import joblib
from flask import Flask, request, jsonify
from flask_cors import CORS
import pandas as pd
import tensorflow as tf

app = Flask(__name__)
CORS(app)

app = Flask(__name__)

lm_model = tf.keras.models.load_model('lm_model.h5')
gc_model = tf.keras.models.load_model('gc_model.h5')
fe_model = tf.keras.models.load_model('fe_model.h5')
lm_scaler = joblib.load('lm_scalar.pkl')
gc_scaler = joblib.load('gc_scalar.pkl')
fe_scaler = joblib.load('scaler.pkl')


def prepare_input_data(scaler,features):
    print("features:",features)
    data = pd.DataFrame([features])
    data_scaled = scaler.transform(data)
    data_reshaped = data_scaled.reshape(data_scaled.shape[0], data_scaled.shape[1], 1)
    return data_reshaped

def make_prediction(scaler,model, features):
    input_data = prepare_input_data(scaler,features)
    prediction = model.predict(input_data)
    return int(prediction[0][0] > 0.5)

@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json(force=True)

    try:
        lm_output = make_prediction(lm_scaler, lm_model, {'LOC_method': data['LOC_method'], 'CC_method': data['CC_method']})
        gc_output = make_prediction(gc_scaler, gc_model, {'WMCNAMM_type': data['WMCNAMM_type'], 'LOC_type': data['LOC_type']})
        fe_output = make_prediction(fe_scaler, fe_model, {'ATFD': data['ATFD_method'], 'LAA': data['LAA_method']})
        print("feOutput:", fe_output)
        return jsonify({
            "lm_output": lm_output,
            "gc_output": gc_output,
            "fe_output": fe_output
        })
    except KeyError as e:
        return jsonify({"error": f"Missing key in input data: {e}"}), 400

if __name__ == "__main__":
    app.run(port=5000, debug=True)



