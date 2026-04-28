package com.sit707.llmchatbot.api;

public interface ApiCallback {
    void onSuccess(String response);
    void onError(String error);
}
