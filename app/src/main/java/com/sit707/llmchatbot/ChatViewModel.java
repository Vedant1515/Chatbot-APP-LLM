package com.sit707.llmchatbot;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sit707.llmchatbot.api.ApiCallback;
import com.sit707.llmchatbot.api.GroqRepository;
import com.sit707.llmchatbot.db.ChatDatabase;
import com.sit707.llmchatbot.db.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final GroqRepository repository;
    private final ChatDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ChatViewModel(@NonNull Application application) {
        super(application);
        db         = ChatDatabase.getInstance(application);
        repository = new GroqRepository();
    }

    public LiveData<List<ChatMessage>> getMessagesLiveData() {
        return messagesLiveData;
    }

    public void loadHistory(String username) {
        executor.execute(() -> {
            List<ChatMessage> messages = db.chatMessageDao().getMessagesByUsername(username);
            if (messages.isEmpty()) {
                ChatMessage welcome = new ChatMessage();
                welcome.setUsername(username);
                welcome.setRole("assistant");
                welcome.setContent("Welcome " + username + "! I'm powered by Groq (Llama 3.3). How can I help you?");
                welcome.setTimestamp(System.currentTimeMillis());
                db.chatMessageDao().insert(welcome);
                messages = new ArrayList<>();
                messages.add(welcome);
            }
            repository.initHistory(messages);
            messagesLiveData.postValue(new ArrayList<>(messages));
        });
    }

    public void sendUserMessage(String text, String username, ApiCallback callback) {
        ChatMessage userMsg = new ChatMessage();
        userMsg.setUsername(username);
        userMsg.setRole("user");
        userMsg.setContent(text);
        userMsg.setTimestamp(System.currentTimeMillis());

        executor.execute(() -> db.chatMessageDao().insert(userMsg));
        appendToLiveData(userMsg);

        repository.sendMessage(text, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                ChatMessage botMsg = new ChatMessage();
                botMsg.setUsername(username);
                botMsg.setRole("assistant");
                botMsg.setContent(response);
                botMsg.setTimestamp(System.currentTimeMillis());

                executor.execute(() -> db.chatMessageDao().insert(botMsg));
                appendToLiveData(botMsg);
                callback.onSuccess(response);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private void appendToLiveData(ChatMessage msg) {
        List<ChatMessage> current = messagesLiveData.getValue();
        List<ChatMessage> updated = current != null ? new ArrayList<>(current) : new ArrayList<>();
        updated.add(msg);
        messagesLiveData.setValue(updated);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
