package com.yusufbatmaz.chatbot.repository;

import com.yusufbatmaz.chatbot.model.ChatHistory;
import com.yusufbatmaz.chatbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, UUID> {
    List<ChatHistory> findByUser(User user);
}
