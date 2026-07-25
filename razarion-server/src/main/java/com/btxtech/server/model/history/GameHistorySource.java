package com.btxtech.server.model.history;

/**
 * Who caused a history event: a human player or a bot. Ported from the old controltheland
 * {@code DbHistoryElement.Source}. Lets the debug log distinguish bot activity from player activity.
 */
public enum GameHistorySource {
    HUMAN,
    BOT
}
