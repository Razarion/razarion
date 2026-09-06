package com.btxtech.server.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Entity tags, strong and weak.
 * <p>
 * The material payloads carry a weak tag now, because Tomcat refuses to compress a response that
 * carries a strong one - eight megabytes of JSON went out unpacked for weeks on that rule alone.
 * What must not break in the process is the comparison: a browser holding the current copy has to
 * be answered 304, and a browser holding an old one must never be. The first mistake costs bytes,
 * the second shows a player the wrong world.
 */
class ContentDigestTest {
    private static final String DIGEST = "0123456789abcdef";

    @Test
    void weakAndStrongCarryTheSameDigest() {
        assertEquals("\"" + DIGEST + "\"", ContentDigest.eTag(DIGEST));
        assertEquals("W/\"" + DIGEST + "\"", ContentDigest.weakETag(DIGEST));
    }

    @Test
    void matchesItsOwnTagInEitherStrength() {
        // The server may hand out either form; a client sends back what it was given.
        assertTrue(ContentDigest.matches(ContentDigest.weakETag(DIGEST), ContentDigest.weakETag(DIGEST)));
        assertTrue(ContentDigest.matches(ContentDigest.eTag(DIGEST), ContentDigest.eTag(DIGEST)));
    }

    @Test
    void ignoresTheWeakPrefixOnEitherSide() {
        // A proxy may weaken a tag on its way through, and the endpoint changed which form it
        // sends - neither may turn a hit into a miss and re-send eight megabytes.
        assertTrue(ContentDigest.matches("W/\"" + DIGEST + "\"", ContentDigest.eTag(DIGEST)));
        assertTrue(ContentDigest.matches("\"" + DIGEST + "\"", ContentDigest.weakETag(DIGEST)));
    }

    @Test
    void findsItsTagInAList() {
        assertTrue(ContentDigest.matches("\"other\", W/\"" + DIGEST + "\" ,\"third\"",
                ContentDigest.weakETag(DIGEST)));
    }

    @Test
    void acceptsTheWildcard() {
        assertTrue(ContentDigest.matches("*", ContentDigest.weakETag(DIGEST)));
    }

    @Test
    void refusesADifferentOrMissingTag() {
        // The direction that matters: doubt costs bytes, never freshness.
        assertFalse(ContentDigest.matches("\"something-else\"", ContentDigest.weakETag(DIGEST)));
        assertFalse(ContentDigest.matches(null, ContentDigest.weakETag(DIGEST)));
        assertFalse(ContentDigest.matches("", ContentDigest.weakETag(DIGEST)));
        assertFalse(ContentDigest.matches("   ", ContentDigest.weakETag(DIGEST)));
    }

    @Test
    void doesNotMatchOnThePrefixAlone() {
        // "W/" stripped from both sides must not make two different digests look alike.
        assertFalse(ContentDigest.matches("W/\"0123456789abcde\"", ContentDigest.weakETag(DIGEST)));
    }

    @Test
    void digestsTheContentNotTheReference() {
        assertEquals(ContentDigest.of("abc".getBytes()), ContentDigest.of("abc".getBytes()));
        assertFalse(ContentDigest.of("abc".getBytes()).equals(ContentDigest.of("abd".getBytes())));
    }
}
