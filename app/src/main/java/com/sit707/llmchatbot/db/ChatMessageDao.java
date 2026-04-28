package com.sit707.llmchatbot.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {
    @Insert
    void insert(ChatMessage message);

    @Query("SELECT * FROM chat_messages WHERE username = :username ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesByUsername(String username);

    @Query("DELETE FROM chat_messages")
    void deleteAll();
}
