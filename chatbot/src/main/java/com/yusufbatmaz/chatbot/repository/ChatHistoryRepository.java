package com.yusufbatmaz.chatbot.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yusufbatmaz.chatbot.model.ChatHistory;
import com.yusufbatmaz.chatbot.model.User;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, UUID> {
    List<ChatHistory> findByUser(User user);
}
