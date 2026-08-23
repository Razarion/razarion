package com.btxtech.server.service.tracking;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * How many visitors a pile of records describes. Getting this wrong is not a visible failure but a
 * plausible wrong number: one boot of a cookie-less browser arrives as a dozen http sessions of one
 * beacon each, and counted per session it reads as a dozen people who opened the game and all got
 * stuck at the first step.
 */
class VisitorGroupsTest {
    @Test
    void recordsSharingNothingAreDifferentVisitors() {
        VisitorGroups groups = new VisitorGroups();
        groups.join(List.of("session:a"));
        groups.join(List.of("session:b"));

        assertNotEquals(groups.keyOf(List.of("session:a")), groups.keyOf(List.of("session:b")));
    }

    @Test
    void sessionsSharingAClickIdAreOneVisitor() {
        VisitorGroups groups = new VisitorGroups();
        groups.join(List.of("twclid:tw-1", "session:a"));
        groups.join(List.of("twclid:tw-1", "session:b"));

        assertEquals(groups.keyOf(List.of("session:a")), groups.keyOf(List.of("session:b")));
    }

    @Test
    void sessionsSharingAGameSessionAreOneVisitor() {
        VisitorGroups groups = new VisitorGroups();
        groups.join(List.of("game:g-1", "session:a"));
        groups.join(List.of("game:g-1", "session:b"));

        assertEquals(groups.keyOf(List.of("session:a")), groups.keyOf(List.of("session:b")));
    }

    /** The link between two records may only be stated by a third, so the chain has to be followed. */
    @Test
    void aChainOfStatementsIsFollowedToTheEnd() {
        VisitorGroups groups = new VisitorGroups();
        groups.join(List.of("session:a", "twclid:tw-1"));
        groups.join(List.of("twclid:tw-1", "game:g-1"));
        groups.join(List.of("game:g-1", "session:b"));

        assertEquals(groups.keyOf(List.of("session:a")), groups.keyOf(List.of("session:b")));
    }

    /** An absent key matches every other absent key, so a record naming nobody names nobody. */
    @Test
    void aRecordWithoutAnyHandleIsNotAVisitor() {
        assertNull(new VisitorGroups().keyOf(List.of()));
    }
}
