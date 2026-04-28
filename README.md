# SIT707 Task 8.1 - LLM ChatBot (Android)

## Overview

A native Android chatbot application built entirely in Java that integrates the Groq API to deliver fast AI-powered conversations. Users sign in with a username and chat with a Llama 3.3 70B model. Every message is persisted locally in a Room (SQLite) database so conversation history is restored automatically on the next session.

---

## Features

- Username-based login with SharedPreferences persistence
- Real-time AI chat powered by the Groq API (Llama 3.3 70B Versatile)
- Full conversation history sent to the model on every request for context-aware replies
- Message history saved locally with Room database, restored on app relaunch
- Chat bubbles with timestamps (HH:mm format)
- User avatar showing the first letter of the username; bot avatar showing "B"
- Welcome message generated on first session for a new username
- Error bubbles showing the real HTTP error message for easy debugging
- Send button disabled while a request is in-flight to prevent duplicate sends
- Keyboard-aware layout using windowSoftInputMode adjustResize

---

## Screenshots

> Add screenshots here after running the app.

---

## Tech Stack

| Category | Library / Tool |
|---|---|
| Language | Java (100% - no Kotlin) |
| Networking | Retrofit 2.9.0 + OkHttp 4.12.0 |
| JSON Parsing | Gson (via Retrofit converter) |
| Local Database | Room 2.6.1 (annotationProcessor, not kapt) |
| AI Model | Groq API - llama-3.3-70b-versatile |
| Threading | ExecutorService (plain Java threads) |
| Reactive UI | LiveData + AndroidViewModel |
| View Access | ViewBinding |
| UI Components | RecyclerView, CardView, Material Components |
| Build System | Gradle (Groovy DSL), AGP 8.7.3 |

---

## Project Structure

```
app/src/main/java/com/sit707/llmchatbot/
|
|-- MainActivity.java         Login screen (username input, SharedPreferences)
|-- ChatActivity.java         Chat screen (RecyclerView, send logic, error bubbles)
|-- ChatViewModel.java        AndroidViewModel (LiveData, DB writes, API calls)
|-- ChatAdapter.java          RecyclerView adapter with two view types (user / bot)
|
|-- api/
|   |-- ApiConfig.java        API key, base URL, model name, max tokens
|   |-- ApiCallback.java      Callback interface (onSuccess / onError)
|   |-- GroqApiService.java   Retrofit interface (@POST chat/completions)
|   |-- RetrofitClient.java   Retrofit + OkHttp singleton
|   |-- GroqRepository.java   Conversation history management, API enqueue, error body reading
|   |
|   `-- model/
|       |-- GroqMessage.java  POJO: role (user/assistant) + content
|       |-- GroqRequest.java  POJO: model, messages list, max_tokens
|       |-- GroqChoice.java   POJO: message field from response choices array
|       `-- GroqResponse.java POJO: choices list from Groq response
|
`-- db/
    |-- ChatMessage.java      Room @Entity (id, username, role, content, timestamp)
    |-- ChatMessageDao.java   Room @Dao (insert, getByUsername, deleteAll)
    `-- ChatDatabase.java     Room @Database singleton with fallbackToDestructiveMigration
```

---

## Architecture

The app follows the MVVM (Model-View-ViewModel) pattern.

```
View (Activity)
    |
    | observes LiveData
    v
ViewModel (ChatViewModel)
    |                        |
    | background thread      | main thread (Retrofit enqueue callback)
    v                        v
Room Database          GroqRepository
(ChatDatabase)         (conversation history + HTTP call)
```

**Data flow for sending a message:**

1. User taps send in ChatActivity
2. ChatActivity calls ChatViewModel.sendUserMessage()
3. ViewModel creates a ChatMessage, appends it to LiveData (bubble appears instantly)
4. ViewModel inserts the message to Room on an ExecutorService background thread
5. ViewModel calls GroqRepository.sendMessage()
6. Repository appends the user turn to its internal conversation history list
7. Repository calls Retrofit enqueue() with the full history as the request body
8. Retrofit dispatches the callback on the main thread
9. Repository extracts choices[0].message.content from the response
10. Repository calls ApiCallback.onSuccess() with the reply text
11. ViewModel creates a bot ChatMessage, appends to LiveData, inserts to Room
12. ChatActivity observer fires, adapter updates, RecyclerView scrolls to bottom

---

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- Android device or emulator running API 24 (Android 7.0) or higher
- A Groq API key (free at https://console.groq.com)

### Steps

1. Clone the repository

```bash
git clone https://github.com/Vedant1515/LLM-Chatbot-APP.git
cd LLM-Chatbot-APP
```

2. Open the project in Android Studio

3. Open the API config file

```
app/src/main/java/com/sit707/llmchatbot/api/ApiConfig.java
```

4. Replace the API key value with your own Groq key

```java
public static final String API_KEY = "your_groq_api_key_here";
```

5. (Optional) Change the model in the same file

```java
public static final String MODEL = "llama-3.3-70b-versatile";
// Other available Groq models:
// "llama3-8b-8192"
// "mixtral-8x7b-32768"
// "gemma2-9b-it"
```

6. Sync Gradle (File - Sync Project with Gradle Files)

7. Run on emulator or physical device

---

## API Details

### Provider

Groq - https://api.groq.com

Groq uses an OpenAI-compatible REST API, so the request and response format matches the OpenAI Chat Completions specification.

### Endpoint

```
POST https://api.groq.com/openai/v1/chat/completions
```

### Authentication

The API key is sent as a Bearer token in the Authorization header:

```
Authorization: Bearer gsk_...
```

### Request Format

```json
{
  "model": "llama-3.3-70b-versatile",
  "max_tokens": 1024,
  "messages": [
    { "role": "user", "content": "Hello" },
    { "role": "assistant", "content": "Hi! How can I help?" },
    { "role": "user", "content": "What is machine learning?" }
  ]
}
```

The full conversation history is included in every request so the model has context for follow-up questions.

### Response Format

```json
{
  "choices": [
    {
      "message": {
        "role": "assistant",
        "content": "Machine learning is..."
      }
    }
  ]
}
```

---

## Database Schema

Table name: `chat_messages`

| Column | Type | Description |
|---|---|---|
| id | INTEGER | Auto-generated primary key |
| username | TEXT | Username from login screen |
| role | TEXT | "user" or "assistant" |
| content | TEXT | Message text |
| timestamp | INTEGER | Unix timestamp in milliseconds |

Messages are queried by username and ordered by timestamp ascending, so history loads in the correct order.

---

## Key Design Decisions

**Why ExecutorService instead of coroutines or RxJava?**
The project requirement specifies Java only. ExecutorService is the standard Java approach for background threading and requires no additional dependencies.

**Why is conversation history stored in the repository and not the database?**
Room queries are async and slow. The repository keeps an in-memory list of GroqMessage objects that is pre-loaded from the database once at session start. This avoids a DB read on every API call.

**Why does history skip leading assistant messages?**
The Groq (and OpenAI) API requires the first message in the messages array to have role "user". The welcome message stored in the database has role "assistant", so it is excluded from the API history while still being displayed in the UI.

**Why is the error bubble not saved to the database?**
Error messages are ephemeral feedback, not real conversation turns. Saving them would corrupt the conversation history sent to the AI model.

---

## Dependencies

```groovy
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
implementation 'androidx.room:room-runtime:2.6.1'
annotationProcessor 'androidx.room:room-compiler:2.6.1'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'com.google.android.material:material:1.12.0'
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
```

---

## License

MIT License

Copyright (c) 2026 Vedant

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
