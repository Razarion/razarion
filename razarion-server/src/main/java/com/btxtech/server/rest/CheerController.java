package com.btxtech.server.rest;

import com.btxtech.server.service.CheerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * A greeting to a player from a line of the community ticker, or one back - see {@link CheerService}.
 * The greetings for the asking player come with the presence poll.
 */
@RestController
@RequestMapping("/rest/cheer")
public class CheerController {
    private final CheerService cheerService;

    public CheerController(CheerService cheerService) {
        this.cheerService = cheerService;
    }

    /** Exactly one of the two: the ticker line greeted, or the greeting answered. */
    public record CheerRequest(Long tickerSeq, Long replyTo) {
    }

    /** Answers {"result": SENT | UNKNOWN | NOT_ONLINE | YOURSELF | ALREADY | TOO_MANY | NOT_IN_GAME}. */
    @PostMapping
    public Map<String, String> send(@RequestBody CheerRequest request) {
        CheerService.Result result;
        if (request.replyTo() != null) {
            result = cheerService.answer(request.replyTo());
        } else if (request.tickerSeq() != null) {
            result = cheerService.greet(request.tickerSeq());
        } else {
            result = CheerService.Result.UNKNOWN;
        }
        return Map.of("result", result.name());
    }
}
