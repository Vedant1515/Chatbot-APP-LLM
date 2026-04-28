# SIT707 Task 8.1 - LLM ChatBot (Android)

A native Android chatbot app built in Java that connects to the Groq API (Llama 3.3 70B). Users log in with a username and chat with the AI. Conversation history is saved locally with Room and restored on relaunch.

## Features

- Username-based login with SharedPreferences
- Real-time AI chat via Groq API (Llama 3.3 70B)
- Full conversation history sent on every request for context
- Message history persisted with Room (SQLite)
- Chat bubbles with timestamps and avatars

## Tech Stack

| Category | Library |
|---|---|
| Language | Java (no Kotlin) |
| Networking | Retrofit 2.9.0 + OkHttp 4.12.0 |
| Local Database | Room 2.6.1 |
| AI Model | Groq - llama-3.3-70b-versatile |
| Architecture | MVVM (ViewModel + LiveData) |

## Setup

1. Clone the repo and open in Android Studio
2. Open `app/src/main/java/com/sit707/llmchatbot/api/ApiConfig.java`
3. Replace the API key:
   ```java
   public static final String API_KEY = "your_groq_api_key_here";
   ```
4. Get a free Groq API key at https://console.groq.com
5. Sync Gradle and run on a device/emulator (API 24+)

## License

MIT License - Copyright (c) 2026 Vedant
