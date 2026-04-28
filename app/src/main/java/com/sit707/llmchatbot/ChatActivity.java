package com.sit707.llmchatbot;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sit707.llmchatbot.api.ApiCallback;
import com.sit707.llmchatbot.databinding.ActivityChatBinding;
import com.sit707.llmchatbot.db.ChatMessage;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private ChatAdapter adapter;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) username = "User";

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(username);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new ChatAdapter(username);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);

        binding.btnSend.setEnabled(false);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        viewModel.getMessagesLiveData().observe(this, messages -> {
            adapter.setMessages(messages);
            binding.btnSend.setEnabled(true);
            if (!messages.isEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size() - 1);
            }
        });

        viewModel.loadHistory(username);

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        binding.etMessage.setText("");
        binding.btnSend.setEnabled(false);

        viewModel.sendUserMessage(text, username, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                binding.btnSend.setEnabled(true);
            }

            @Override
            public void onError(String error) {
                // Show the real error so it's easy to diagnose
                Log.e(TAG, "Gemini error: " + error);
                showErrorBubble(error);
                binding.btnSend.setEnabled(true);
            }
        });
    }

    private void showErrorBubble(String errorDetail) {
        ChatMessage errorMsg = new ChatMessage();
        errorMsg.setRole("assistant");
        errorMsg.setUsername(username);
        errorMsg.setContent("⚠ " + errorDetail);
        errorMsg.setTimestamp(System.currentTimeMillis());
        adapter.addMessage(errorMsg);
        binding.rvMessages.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
