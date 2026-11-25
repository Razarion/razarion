package com.btxtech.server.rest;

import com.btxtech.server.service.EmoteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** A sign over one of the player's own units - see {@link EmoteService}. */
@RestController
@RequestMapping("/rest/emote")
public class EmoteController {
    private final EmoteService emoteService;

    public EmoteController(EmoteService emoteService) {
        this.emoteService = emoteService;
    }

    public record EmoteRequest(String emote, int syncItemId, boolean reply) {
    }

    /** Answers {"result": SENT | UNKNOWN_EMOTE | NOT_YOUR_UNIT | TOO_MANY | NOT_IN_GAME}. */
    @PostMapping
    public Map<String, String> send(@RequestBody EmoteRequest request) {
        return Map.of("result", emoteService.send(request.emote(), request.syncItemId(), request.reply()).name());
    }
}
