package com.btxtech.server.rest.editor;

import com.btxtech.server.model.history.GameHistorySummaryRow;
import com.btxtech.server.service.history.HistoryService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * What happened in the world over the last days, as counts. Read by the social pipeline's weekly
 * summary post (razarion-social/pipeline/lib/formats/week-in-numbers.mjs).
 */
@RestController
@RequestMapping("/rest/editor/game-history-summary")
public class GameHistorySummaryController {
    /** The longest any event is kept. Item and base events cover seven days whatever is asked for. */
    private static final int MAX_DAYS = 90;
    private final HistoryService historyService;

    public GameHistorySummaryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GameHistorySummaryRow> summarize(@RequestParam(value = "days", defaultValue = "7") int days) {
        int clamped = Math.max(1, Math.min(days, MAX_DAYS));
        return historyService.summarize(new Date(System.currentTimeMillis() - clamped * 86_400_000L));
    }
}
