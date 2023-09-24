from flask import Flask, request, jsonify
from flask_cors import CORS
import numpy as np
import tensorflow as tf

app = Flask(__name__)
CORS(app)  # This will handle CORS headers, allowing your Java app to make requests.

# Load the trained model
model = tf.keras.models.load_model('long_method72.h5')


@app.route('/predict', methods=['POST'])
def predict():
    # Extract features from POST request
    data = request.get_json(force=True)

    # Convert the features into an array format.
    input_data = np.array([
        data['LOC'],
        data['numVariables'],
        data['numLoops'],
        data['numComments'],
        data['numConditions'],
        data['cyclomaticComplexity']
    ])

    # Reshape the data for the model (if required).
    # For a dense neural network, you might only need to reshape it to (-1, number_of_features)
    input_data = input_data.reshape(1, -1)

    # Make a prediction
    prediction = model.predict(input_data)

    # Convert the prediction to your desired format
    output = 1 if prediction[0][0] > 0.5 else 0

    # output = int(prediction[0][0] > 0.5)

    return jsonify(output)


if __name__ == "__main__":
    app.run(port=5000, debug=True)
