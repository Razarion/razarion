package com.btxtech.server.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Entity tags for the few responses that are large, rarely changed, and asked for on every single
 * game start: the model file and the terrain height map. Together they are the better part of what
 * a player downloads before anything happens.
 * <p>
 * They live under /rest/, where {@code NoCacheRestFilter} marks everything no-store - which forbids
 * a browser from keeping the file at all. The guarantee behind that header is real: an edited model
 * or a re-uploaded height map has to reach the player. {@code no-cache} plus one of these tags keeps
 * the guarantee and drops the cost, because the browser may hold the file but must ask before every
 * use, and the answer is a comparison rather than a download.
 * <p>
 * The digest is stored beside the bytes it describes, written in the same transaction. Not computed
 * per request: the blobs are lazily fetched and megabytes long, so answering "unchanged?" must not
 * have to load one. Not cached per process either - a second pod would answer from a digest it
 * computed before the content was replaced, which is exactly the staleness this exists to prevent.
 */
public final class ContentDigest {
    private ContentDigest() {
    }

    /** SHA-256 of the content, hex. Changes exactly when the bytes change. */
    public static String of(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required of every Java platform; if it is gone, something far worse is
            // wrong than a cache header.
            throw new IllegalStateException(e);
        }
    }

    /** The digest as an HTTP entity tag - quoted, strong. */
    public static String eTag(String digest) {
        return "\"" + digest + "\"";
    }

    /**
     * Whether the client already holds exactly this content. {@code If-None-Match} may carry a list
     * and may weaken a tag with a {@code W/} prefix; {@code *} means "any copy at all".
     * <p>
     * Anything unrecognised falls through to a full response. Answering 304 by mistake is the one
     * error that shows a player the wrong world, so the doubt always costs bytes, never freshness.
     */
    public static boolean matches(String ifNoneMatch, String eTag) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank()) {
            return false;
        }
        for (String candidate : ifNoneMatch.split(",")) {
            String trimmed = candidate.trim();
            if (trimmed.startsWith("W/")) {
                trimmed = trimmed.substring(2);
            }
            if (trimmed.equals("*") || trimmed.equals(eTag)) {
                return true;
            }
        }
        return false;
    }
}
