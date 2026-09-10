package com.btxtech.server.rest.ui;

import com.btxtech.server.service.ui.GltfService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Eleven megabytes on every game start, because no-store forbade keeping the file at all. The
 * replacement has to hold two things at once: a browser that already has the current model sends
 * nothing over the wire, and a browser holding yesterday's model can never be told it is current.
 * The second one is why every uncertain case here resolves towards sending the file.
 */
class GltfControllerTest {
    private static final byte[] GLB = new byte[]{1, 2, 3};
    private static final String DIGEST = "0123456789abcdef";
    private static final String E_TAG = "\"" + DIGEST + "\"";

    private final GltfService gltfService = mock(GltfService.class);
    private final GltfController controller = new GltfController(gltfService);

    private ResponseEntity<Resource> get(String ifNoneMatch) {
        when(gltfService.getGlbDigest(1)).thenReturn(DIGEST);
        when(gltfService.getGlb(1)).thenReturn(GLB);
        return controller.getGlb(1, ifNoneMatch);
    }

    @Test
    void aBrowserWithoutACopyGetsTheModelAndItsTag() {
        ResponseEntity<Resource> response = get(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(GLB, bytes(response));
        assertEquals(E_TAG, response.getHeaders().getETag());
    }

    /**
     * The whole point: the same model twice costs one round trip instead of eleven megabytes. The
     * blob must not even be read - that is what makes a revalidation cheap on the server too.
     */
    @Test
    void aBrowserHoldingTheSameModelIsToldSoAndGetsNoBytes() {
        ResponseEntity<Resource> response = get(E_TAG);

        assertEquals(HttpStatus.NOT_MODIFIED, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(E_TAG, response.getHeaders().getETag());
        verify(gltfService, never()).getGlb(anyInt());
    }

    /** The case the no-store header was protecting. It has to keep working. */
    @Test
    void aBrowserHoldingAnOlderModelGetsTheNewOne() {
        ResponseEntity<Resource> response = get("\"an-older-digest\"");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(GLB, bytes(response));
    }

    /**
     * The header may carry several tags, and a proxy may have weakened one. A weak tag is still a
     * statement about which bytes are held, and for a file that is either identical or replaced
     * wholesale it means the same thing.
     */
    @Test
    void aListOfTagsAndAWeakenedTagAreBothUnderstood() {
        assertEquals(HttpStatus.NOT_MODIFIED, get("\"other\", W/" + E_TAG).getStatusCode());
    }

    /** A cache asking "have you got anything at all" is answered by what we have. */
    @Test
    void theWildcardIsAccepted() {
        assertEquals(HttpStatus.NOT_MODIFIED, get("*").getStatusCode());
    }

    /**
     * Anything unparsed sends the file. Guessing wrong in this direction costs bandwidth; guessing
     * wrong in the other shows a player a world that no longer exists.
     */
    @Test
    void anythingNotUnderstoodCostsBytesRatherThanFreshness() {
        assertEquals(HttpStatus.OK, get("").getStatusCode());
        assertEquals(HttpStatus.OK, get("   ").getStatusCode());
        assertEquals(HttpStatus.OK, get("garbage").getStatusCode());
        assertEquals(HttpStatus.OK, get(DIGEST).getStatusCode());
    }

    /**
     * The header that replaces no-store. It has to say "ask every time" - a max-age of any size
     * would let a player keep an old model for that long.
     */
    @Test
    void theResponseSaysAskBeforeEveryUse() {
        String cacheControl = get(null).getHeaders().getCacheControl();

        assertNotNull(cacheControl);
        assertEquals("no-cache, must-revalidate", cacheControl);
        assertNull(get(E_TAG).getHeaders().getFirst(HttpHeaders.EXPIRES));
    }

    /** A row with no model at all still answers, as it did before there were tags. */
    @Test
    void aModelThatIsNotThereIsNotTagged() {
        when(gltfService.getGlbDigest(7)).thenReturn(null);
        when(gltfService.getGlb(7)).thenReturn(null);

        ResponseEntity<Resource> response = controller.getGlb(7, E_TAG);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getHeaders().getETag());
        assertNull(response.getBody());
    }

    private static byte[] bytes(ResponseEntity<Resource> response) {
        try {
            return response.getBody().getContentAsByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // ---- The range tests below are the guard for a cliff that cost us every edge cache hit. ----

    /**
     * Why these exist.
     * <p>
     * Cloud CDN stores a response larger than 10 MiB only if the origin can serve byte ranges,
     * because ranges are how it fills its cache in chunks. This endpoint returned a {@code byte[]},
     * for which Spring MVC writes the whole body and never mentions ranges - so the 10.83 MiB model
     * was never cached at any edge. Measured over 72 hours: 685 requests to the immutable digest
     * url, 458 complete deliveries, cacheLookup true on every one, and zero cache fills. Nothing
     * failed and nothing was logged; the file was simply always fetched from us-central1.
     * <p>
     * Returning a {@link Resource} is what fixes it, and it is a one-word change that a later
     * refactor could undo just as quietly. Hence a test that fails loudly if the response stops
     * being range-capable.
     */
    @Test
    void theResponseAdvertisesThatItCanServeRanges() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders.get("/rest/gltf/glb/1"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                .andExpect(content().bytes(GLB));
    }

    /** And actually serves one, rather than only claiming to. */
    @Test
    void aRangeRequestIsAnsweredWithThatRange() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders.get("/rest/gltf/glb/1").header(HttpHeaders.RANGE, "bytes=1-2"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 1-2/3"))
                .andExpect(content().bytes(new byte[]{2, 3}));
    }

    /** The digest url is the one the CDN is meant to hold, so it is the one that matters most. */
    @Test
    void theDigestUrlServesRangesToo() throws Exception {
        when(gltfService.getGlbDigest(1)).thenReturn(DIGEST);
        when(gltfService.getGlb(1)).thenReturn(GLB);

        mockMvc().perform(MockMvcRequestBuilders.get("/rest/gltf/glb/1/" + DIGEST)
                        .header(HttpHeaders.RANGE, "bytes=0-0"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 0-0/3"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=31536000, public, immutable"))
                .andExpect(content().bytes(new byte[]{1}));
    }

    /**
     * Standalone rather than a full context: this is about what Spring's return value handling does
     * with a Resource, which needs the real message converters and nothing else. A booted
     * application would drag in the database and the game engine to answer a question about
     * headers.
     */
    private MockMvc mockMvc() {
        when(gltfService.getGlbDigest(1)).thenReturn(DIGEST);
        when(gltfService.getGlb(1)).thenReturn(GLB);
        return MockMvcBuilders.standaloneSetup(controller).build();
    }
}
