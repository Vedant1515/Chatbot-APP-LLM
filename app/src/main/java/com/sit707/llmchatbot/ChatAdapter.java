package com.sit707.llmchatbot;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sit707.llmchatbot.databinding.ItemMessageBotBinding;
import com.sit707.llmchatbot.databinding.ItemMessageUserBinding;
import com.sit707.llmchatbot.db.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_BOT = 0;
    private static final int VIEW_TYPE_USER = 1;

    private final List<ChatMessage> messages = new ArrayList<>();
    private final String username;

    public ChatAdapter(String username) {
        this.username = username;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getRole().equals("user") ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<ChatMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            return new UserViewHolder(ItemMessageUserBinding.inflate(inflater, parent, false));
        }
        return new BotViewHolder(ItemMessageBotBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(msg.getTimestamp()));

        if (holder instanceof UserViewHolder) {
            UserViewHolder uvh = (UserViewHolder) holder;
            uvh.binding.tvMessage.setText(msg.getContent());
            uvh.binding.tvTimestamp.setText(time);
            String initial = username.length() > 0
                    ? String.valueOf(Character.toUpperCase(username.charAt(0)))
                    : "U";
            uvh.binding.tvAvatar.setText(initial);
        } else {
            BotViewHolder bvh = (BotViewHolder) holder;
            bvh.binding.tvMessage.setText(msg.getContent());
            bvh.binding.tvTimestamp.setText(time);
            bvh.binding.tvAvatar.setText("B");
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        final ItemMessageUserBinding binding;

        UserViewHolder(ItemMessageUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        final ItemMessageBotBinding binding;

        BotViewHolder(ItemMessageBotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
