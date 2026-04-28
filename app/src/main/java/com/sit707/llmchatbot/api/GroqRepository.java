package com.sit707.llmchatbot.api;

import com.sit707.llmchatbot.api.model.GroqChoice;
import com.sit707.llmchatbot.api.model.GroqMessage;
import com.sit707.llmchatbot.api.model.GroqRequest;
import com.sit707.llmchatbot.api.model.GroqResponse;
import com.sit707.llmchatbot.db.ChatMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroqRepository {

    private final List<GroqMessage> conversationHistory = new ArrayList<>();
    private final GroqApiService apiService;

    public GroqRepository() {
        apiService = RetrofitClient.getInstance().getApiService();
    }

    // Re-hydrate conversation history from Room on session restore.
    // Skip any leading assistant-only turns — Groq requires the first message to be "user".
    public void initHistory(List<ChatMessage> chatMessages) {
        conversationHistory.clear();
        boolean foundUser = false;
        for (ChatMessage msg : chatMessages) {
            if (msg.getRole().equals("user")) foundUser = true;
            if (foundUser) {
                conversationHistory.add(new GroqMessage(msg.getRole(), msg.getContent()));
            }
        }
    }

    public void sendMessage(String userText, ApiCallback callback) {
        conversationHistory.add(new GroqMessage("user", userText));

        GroqRequest request = new GroqRequest(
                ApiConfig.MODEL,
                new ArrayList<>(conversationHistory),
                ApiConfig.MAX_TOKENS
        );

        apiService.createChatCompletion("Bearer " + ApiConfig.API_KEY, request)
                .enqueue(new Callback<GroqResponse>() {
                    @Override
                    public void onResponse(Call<GroqResponse> call, Response<GroqResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<GroqChoice> choices = response.body().getChoices();
                            if (choices != null && !choices.isEmpty()
                                    && choices.get(0).getMessage() != null) {
                                String reply = choices.get(0).getMessage().getContent();
                                conversationHistory.add(new GroqMessage("assistant", reply));
                                callback.onSuccess(reply);
                                return;
                            }
                        }
                        String errorBody = "";
                        try {
                            if (response.errorBody() != null) {
                                errorBody = response.errorBody().string();
                            }
                        } catch (IOException ignored) {}
                        callback.onError("HTTP " + response.code() + " — " + errorBody);
                    }

                    @Override
                    public void onFailure(Call<GroqResponse> call, Throwable t) {
                        callback.onError(t.getMessage() != null ? t.getMessage() : "Network error");
                    }
                });
    }
}
