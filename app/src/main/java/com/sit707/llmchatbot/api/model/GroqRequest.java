package com.sit707.llmchatbot.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GroqRequest {
    private String model;
    private List<GroqMessage> messages;

    @SerializedName("max_tokens")
    private int maxTokens;

    public GroqRequest(String model, List<GroqMessage> messages, int maxTokens) {
        this.model     = model;
        this.messages  = messages;
        this.maxTokens = maxTokens;
    }

    public String getModel()               { return model; }
    public List<GroqMessage> getMessages() { return messages; }
    public int getMaxTokens()              { return maxTokens; }
}
