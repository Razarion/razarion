package com.btxtech.server.rest;

import com.btxtech.server.service.ChatService;
import com.btxtech.shared.datatypes.ChatMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    /** The backend chat tab: what was written in a span, with user ids, the assistant's answers included. */
    @GetMapping(value = "admin", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<ChatService.Entry> admin(@RequestParam long from, @RequestParam long to) {
        return chatService.between(new Date(from), new Date(to));
    }

    /**
     * A clip of the chat, loaded only when a player clicks it. A ByteArrayResource, so a range request
     * (Safari asks for nothing else for a video) is answered with that part. A clip never changes.
     */
    @GetMapping(value = "video/{id}")
    public ResponseEntity<Resource> video(@PathVariable("id") int id) {
        byte[] data = chatService.videoData(id);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic().immutable())
                .body(new ByteArrayResource(data));
    }
}
