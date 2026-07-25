package com.btxtech.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.tomcat.autoconfigure.servlet.TomcatServletWebServerAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * How the Angular bundle leaves the server. Both properties below were wrong in production for a
 * long time without anything failing - a missing mime type and a missing cache header do not
 * throw, they just make every game start download several megabytes it did not have to:
 * <ul>
 * <li>{@code .js} is served as {@code text/javascript}. While only {@code application/javascript}
 * was listed under server.compression.mime-types, the whole bundle went out uncompressed -
 * 5.6 MB instead of 1.3 MB.</li>
 * <li>The hashed build output was served with no Cache-Control at all, so browsers re-fetched
 * it on heuristics instead of keeping it.</li>
 * </ul>
 * The test boots the real application.properties and the real {@link WebMvcConfiguration} over a
 * plain web context - no database, no security - and asks for a real file from the build output.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = StaticAssetDeliveryTest.WebOnlyApplication.class)
class StaticAssetDeliveryTest {
    /**
     * Only the web layer, listed explicitly rather than auto configured: this test is about how
     * files are served, and everything else in the application would want a database.
     * application.properties is still read in full, so the settings under test are the real ones.
     */
    @Configuration
    @ImportAutoConfiguration({
            TomcatServletWebServerAutoConfiguration.class,
            DispatcherServletAutoConfiguration.class,
            WebMvcAutoConfiguration.class
    })
    @Import(WebMvcConfiguration.class)
    static class WebOnlyApplication {
    }

    @LocalServerPort
    private int port;

    private final RestTemplate client = new RestTemplate();

    @Test
    void hashedJavaScriptIsCompressedAndCachedForever() throws IOException {
        String name = anyBundleFileName();

        ResponseEntity<byte[]> response = get("/game/" + name, "gzip, deflate");

        assertEquals(200, response.getStatusCode().value(), "not served: " + name);
        assertEquals("gzip", response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING),
                "JavaScript went out uncompressed - is text/javascript listed under server.compression.mime-types?");
        String cacheControl = response.getHeaders().getCacheControl();
        assertNotNull(cacheControl, "no Cache-Control on a content hashed file: " + name);
        assertTrue(cacheControl.contains("max-age=31536000") && cacheControl.contains("immutable"),
                "content hashed file should be cacheable forever, was: " + cacheControl);
    }

    /**
     * index.html names the current hashes. Caching it would leave a deployed client asking for
     * chunks that no longer exist.
     */
    @Test
    void indexHtmlIsNotCached() {
        ResponseEntity<byte[]> response = get("/game/index.html", "gzip, deflate");

        assertEquals(200, response.getStatusCode().value());
        String cacheControl = response.getHeaders().getCacheControl();
        assertNotNull(cacheControl, "index.html must say it is not to be cached");
        assertTrue(cacheControl.contains("no-cache"), "was: " + cacheControl);
    }

    private ResponseEntity<byte[]> get(String path, String acceptEncoding) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT_ENCODING, acceptEncoding);
        return client.exchange(new RequestEntity<Void>(headers, HttpMethod.GET,
                URI.create("http://localhost:" + port + path)), byte[].class);
    }

    /**
     * Any file from the Angular build big enough to be compressed at all - the exact hashes
     * change with every build, so the test picks one instead of naming it.
     */
    private String anyBundleFileName() throws IOException {
        for (Resource resource : new PathMatchingResourcePatternResolver()
                .getResources("classpath:/generated/game/*.js")) {
            if (resource.contentLength() > 1024) {
                return resource.getFilename();
            }
        }
        throw new IllegalStateException("No Angular build output in classpath:/generated/game/ - build razarion-frontend first");
    }
}
