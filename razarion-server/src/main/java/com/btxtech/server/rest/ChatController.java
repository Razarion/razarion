package com.btxtech.server.rest;

import com.btxtech.server.service.ChatService;
import com.btxtech.shared.datatypes.ChatMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/chat-controller")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping(value = {"getall"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ChatMessage> getAllMessages() {
        return chatService.getAllMessages();
    }

    @PostMapping(value = "send", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void send(@RequestBody String message) {
        chatService.send(message);
    }
}
