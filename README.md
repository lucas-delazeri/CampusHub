# CampusHub 🎓

CampusHub is a modern Android application designed for university students to discover, manage, and register for campus events. Built with **Jetpack Compose** and **Firebase**, it offers a seamless and interactive experience for student integration.

## 🚀 Features

- **Authentication System**:
    - Email & Password Registration and Login.
    - Google Sign-In integration.
    - Password recovery via email (Forgot Password).
    - Persistent login session (Auto-redirect if already logged in).
- **Event Management**:
    - **Events Feed**: Browse all available university events with real-time search filtering.
    - **Event Details**: Deep dive into event info (Date, Location, Organizer, and Description).
    - **Enrollment**: One-tap registration or cancellation for any event.
    - **My Events**: A dedicated tab to manage your active enrollments.
- **User Profile**:
    - View and edit your full name.
    - Profile picture upload to **Firebase Storage**.
    - Real-time profile updates across the app.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/compose)
- **Design System**: [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: 
    - [Firebase Auth](https://firebase.google.com/docs/auth) (Authentication)
    - [Firebase Storage](https://firebase.google.com/docs/storage) (Image Hosting)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Dependency Management**: Gradle (Kotlin DSL)

## 🏗 Architecture

The project follows the recommended Android architecture guidelines:
- **UI Layer**: Composable functions for declarative UI.
- **ViewModel**: Manages UI state and business logic, ensuring data survival through configuration changes.
- **Data Layer**: Repository pattern for event management and Firebase integration.

## ⚙️ Setup & Installation

To run this project locally:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/campushub.git
   ```

2. **Firebase Configuration**:
    - Create a project in the [Firebase Console](https://console.firebase.google.com/).
    - Enable **Authentication** (Email/Password and Google).
    - Enable **Cloud Storage**.
    - Download the `google-services.json` file and place it in the `app/` directory.

3. **Build & Run**:
    - Open the project in **Android Studio**.
    - Sync Gradle files.
    - Run the application on an emulator or physical device.

## 📄 License

This project is under the MIT License. Feel free to use and contribute!
