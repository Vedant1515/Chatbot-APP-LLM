package com.sit707.llmchatbot.api;

import com.sit707.llmchatbot.api.model.GroqRequest;
import com.sit707.llmchatbot.api.model.GroqResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GroqApiService {
    // Groq is OpenAI-compatible; API key goes in Authorization header.
    @POST("chat/completions")
    Call<GroqResponse> createChatCompletion(
            @Header("Authorization") String authorization,
            @Body GroqRequest request
    );
}
