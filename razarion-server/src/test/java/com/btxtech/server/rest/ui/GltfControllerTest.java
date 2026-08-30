package com.btxtech.server.rest.ui;

import com.btxtech.server.service.ui.GltfService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private ResponseEntity<byte[]> get(String ifNoneMatch) {
        when(gltfService.getGlbDigest(1)).thenReturn(DIGEST);
        when(gltfService.getGlb(1)).thenReturn(GLB);
        return controller.getGlb(1, ifNoneMatch);
    }

    @Test
    void aBrowserWithoutACopyGetsTheModelAndItsTag() {
        ResponseEntity<byte[]> response = get(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(GLB, response.getBody());
        assertEquals(E_TAG, response.getHeaders().getETag());
    }

    /**
     * The whole point: the same model twice costs one round trip instead of eleven megabytes. The
     * blob must not even be read - that is what makes a revalidation cheap on the server too.
     */
    @Test
    void aBrowserHoldingTheSameModelIsToldSoAndGetsNoBytes() {
        ResponseEntity<byte[]> response = get(E_TAG);

        assertEquals(HttpStatus.NOT_MODIFIED, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(E_TAG, response.getHeaders().getETag());
        verify(gltfService, never()).getGlb(anyInt());
    }

    /** The case the no-store header was protecting. It has to keep working. */
    @Test
    void aBrowserHoldingAnOlderModelGetsTheNewOne() {
        ResponseEntity<byte[]> response = get("\"an-older-digest\"");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(GLB, response.getBody());
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

        ResponseEntity<byte[]> response = controller.getGlb(7, E_TAG);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getHeaders().getETag());
    }
}
