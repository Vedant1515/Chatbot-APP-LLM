package com.sit707.llmchatbot.api.model;

public class GroqMessage {
    private String role;      // "user" | "assistant" | "system"
    private String content;

    public GroqMessage(String role, String content) {
        this.role    = role;
        this.content = content;
    }

    public String getRole()    { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
