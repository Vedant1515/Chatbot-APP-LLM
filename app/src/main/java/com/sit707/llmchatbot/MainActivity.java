package com.sit707.llmchatbot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sit707.llmchatbot.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String PREFS_NAME = "ChatBotPrefs";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Pre-fill saved username
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String saved = prefs.getString(KEY_USERNAME, "");
        if (!saved.isEmpty()) {
            binding.etUsername.setText(saved);
        }

        binding.btnGo.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString(KEY_USERNAME, username).apply();
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });
    }
}
