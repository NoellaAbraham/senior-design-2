from flask import Flask, request, jsonify
import cv2
import numpy as np
from deepface import DeepFace

app = Flask(__name__)

# Load Haar cascade for face detection
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')

@app.route("/")
def hello():
    return "Flask is working!"

@app.route('/predict_emotion', methods=['POST'])
def predict_emotion():
    try:
        # Step 1: Get the image from request
        if 'image' not in request.files:
            return jsonify({'emotion': 'No image received'})

        file = request.files['image']
        img_bytes = np.frombuffer(file.read(), np.uint8)
        frame = cv2.imdecode(img_bytes, cv2.IMREAD_COLOR)

        # Step 2: Convert to grayscale and RGB for processing
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        rgb = cv2.cvtColor(gray, cv2.COLOR_GRAY2RGB)

        # Step 3: Detect faces
        faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30))
        print(f"Detected {len(faces)} faces")

        if len(faces) == 0:
            return jsonify({'emotion': 'No face detected'})

        # Step 4: Crop the first detected face
        x, y, w, h = faces[0]
        face_roi = rgb[y:y + h, x:x + w]

        # Step 5: Run DeepFace emotion analysis
        result = DeepFace.analyze(face_roi, actions=['emotion'], enforce_detection=False)
        emotion = result[0]['dominant_emotion']
        print("Emotion Detected:", emotion)

        return jsonify({'emotion': emotion})

    except Exception as e:
        print("Exception:", e)
        return jsonify({'emotion': 'Error', 'detail': str(e)})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
