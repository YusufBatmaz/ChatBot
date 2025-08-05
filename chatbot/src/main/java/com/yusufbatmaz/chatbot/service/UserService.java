package com.yusufbatmaz.chatbot.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yusufbatmaz.chatbot.model.User;
import com.yusufbatmaz.chatbot.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        user.setId(null); // Kayıt öncesi id'yi sıfırla!
        return userRepository.save(user);
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
