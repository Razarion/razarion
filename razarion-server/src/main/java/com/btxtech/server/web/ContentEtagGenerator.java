package com.btxtech.server.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * ETags from the content of a static file, for the resource handlers in WebMvcConfiguration.
 * <p>
 * The image is built by Jib, which stamps every file with 1970-01-01 00:00:01. Spring sent that as
 * Last-Modified, so "changed since 1970?" was answered 304 for every file after every deploy - the
 * new sprite sheets never reached a browser that had the old ones, and neither did a new
 * /game/index.html (PROD, 2026-10-02). A hash of the bytes changes exactly when the file does.
 * <p>
 * Hashed once per file and kept: the files in a running image do not change. The key carries the
 * modification time and length as well, because the local server serves target/classes, where a
 * frontend rebuild replaces files under a running server.
 */
public class ContentEtagGenerator implements Function<Resource, String> {
    private static final Logger logger = LoggerFactory.getLogger(ContentEtagGenerator.class);
    private final Map<String, String> etags = new ConcurrentHashMap<>();

    @Override
    public String apply(Resource resource) {
        try {
            String key = resource.getURL() + "|" + resource.lastModified() + "|" + resource.contentLength();
            return etags.computeIfAbsent(key, k -> hash(resource));
        } catch (IOException | RuntimeException e) {
            // No ETag is a full download, never a wrong 304.
            logger.warn("No ETag for {}: {}", resource.getDescription(), e.getMessage());
            return null;
        }
    }

    private static String hash(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            return "\"" + HexFormat.of().formatHex(digest.digest()) + "\"";
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
