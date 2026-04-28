package com.sit707.llmchatbot.api.model;

import java.util.List;

public class GroqResponse {
    private List<GroqChoice> choices;

    public List<GroqChoice> getChoices() { return choices; }
    public void setChoices(List<GroqChoice> choices) { this.choices = choices; }
}
