package com.btxtech.server.service;

import com.btxtech.server.model.ChatMessageEntity;
import com.btxtech.server.repository.ChatMessagesRepository;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.ChatMessage;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    private final ChatMessagesRepository chatMessagesRepository;
    private final UserService userService;

    public ChatService(ChatMessagesRepository chatMessagesRepository, UserService userService) {
        this.chatMessagesRepository = chatMessagesRepository;
        this.userService = userService;
    }

    @Transactional
    public List<ChatMessage> getAllMessages() {
        return chatMessagesRepository
                .findAll()
                .stream()
                .map(ChatMessageEntity::toChatMessage)
                .toList();
    }

    @Transactional
    public void send(String message) {
        var userContext = userService.getUserContextFromContext();
        if (!userContext.checkName()) {
            throw new IllegalStateException("The name has not been set for user: " + userContext.getUserId());
        }

        chatMessagesRepository.save(new ChatMessageEntity()
                .init(userContext.getUserId(), userContext.getName(), message));
    }
}
