# Real-Time ASL Fingerspelling Recognizer

A real-time American Sign Language (ASL) fingerspelling recognition system built for Android. The app uses on-device hand landmark tracking and a lightweight neural network to classify ASL letters live from the camera feed — entirely on-device, no server or internet connection required.

📹 **Demo video & screenshots:** [Google Drive folder](https://drive.google.com/drive/folders/1usEGIZk4Ofj1LK_AeBigwp8uOixdkrAm)
📦 **Download:** [v1.0.0 Release APK](https://github.com/manas-0/ASL-Fingerspelling-Analyzer/releases/tag/v1.0.0)

---

## ✨ Features

- **Real-time recognition** of ASL fingerspelling (28-class alphabet) directly from a live camera feed
- **100% on-device** — hand tracking and inference both run locally, no backend or network dependency
- **On-device hand tracking** using MediaPipe HandLandmarker
- **Lightweight custom model** — a Keras MLP trained on normalized hand landmarks, exported as an INT8-quantized TFLite model (~29KB)
- **Confidence thresholding + temporal smoothing** to reduce flicker and false positives on noisy frame-to-frame predictions

---

## 📊 Model Performance

| Metric | Value |
|---|---|
| Validation Accuracy | 98.62% |
| Model Size (TFLite, INT8) | ~29 KB |
| Classes | 28 (ASL alphabet fingerspelling) |
| Inference | Fully on-device |

The classifier is a Keras MLP trained on normalized MediaPipe hand landmark coordinates for the 28-class ASL fingerspelling alphabet. Training was done on **Kaggle Notebooks** due to a local Python 3.14 / TensorFlow incompatibility. The trained model was exported and **INT8-quantized** to TFLite for fast, lightweight on-device inference.

---

## 🏗️ Architecture

```
Camera Feed (CameraX)
      │
      ▼
MediaPipe HandLandmarker  →  21 hand landmarks (x, y, z)
      │
      ▼
Normalization
      │
      ▼
TFLite MLP (INT8 quantized, 29KB)  — runs fully on-device
      │
      ▼
Confidence Thresholding + Temporal Smoothing
      │
      ▼
Predicted Letter (UI)
```

---

## 🛠️ Tech Stack

- Kotlin + Jetpack Compose
- CameraX for camera capture
- MediaPipe HandLandmarker for hand tracking
- TensorFlow Lite for on-device inference

---

## 📥 Getting Started

### Download (fastest way to try it)
Grab the signed APK from **[Releases → v1.0.0](https://github.com/manas-0/ASL-Fingerspelling-Analyzer/releases/tag/v1.0.0)** and sideload it directly onto a device.

- **Minimum Android version:** Android 14 (API 34)
- A physical device with a camera is strongly recommended for real-time performance

### Build from source
```bash
git clone https://github.com/manas-0/ASL-Fingerspelling-Analyzer.git
cd ASL-Fingerspelling-Analyzer
```
1. Open the project in Android Studio.
2. Let Gradle sync and download dependencies (CameraX, MediaPipe, TFLite).
3. Connect a physical Android device (camera-enabled, Android 14+) via USB debugging, or use an emulator with a virtual camera.
4. Run the app.

---

## 🗺️ Roadmap

- [ ] Expand beyond fingerspelling to full ASL word/phrase recognition
- [ ] Add on-device model update pipeline
- [ ] Improve temporal smoothing with a sequence model (e.g. lightweight RNN/GRU)
- [ ] Lower minimum SDK if broader device support is needed

---

## 📄 License


This project is open for personal and educational use. Feel free to explore the code.



---

## 🙋 Contact

**Manas More**
- GitHub: [manas-0](https://github.com/manas-0)
- Email: manas.developer01@gmail.com
