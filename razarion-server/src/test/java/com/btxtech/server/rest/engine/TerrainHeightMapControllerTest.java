package com.btxtech.server.rest.engine;

import com.btxtech.server.service.engine.PlanetCrudService;
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
 * Four megabytes on every game start, and the game engine worker cannot run until they arrive: of
 * the PROD sessions that got their user interface up and never a running game, the recent ones were
 * all waiting on this. The replacement has to hold two things at once - a browser that already has
 * the current height map sends nothing over the wire, and a browser holding yesterday's can never
 * be told it is current, or the player walks on terrain that no longer exists.
 */
class TerrainHeightMapControllerTest {
    private static final byte[] HEIGHT_MAP = new byte[]{1, 2, 3};
    private static final String DIGEST = "0123456789abcdef";
    private static final String E_TAG = "\"" + DIGEST + "\"";

    private final PlanetCrudService planetCrudService = mock(PlanetCrudService.class);
    private final TerrainHeightMapControllerImpl controller = new TerrainHeightMapControllerImpl(planetCrudService);

    private ResponseEntity<byte[]> get(String ifNoneMatch) {
        when(planetCrudService.getCompressedHeightMapDigest(1)).thenReturn(DIGEST);
        when(planetCrudService.getCompressedHeightMap(1)).thenReturn(HEIGHT_MAP);
        return controller.getCompressedHeightMap(1, ifNoneMatch);
    }

    @Test
    void aBrowserWithoutACopyGetsTheHeightMapAndItsTag() {
        ResponseEntity<byte[]> response = get(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(HEIGHT_MAP, response.getBody());
        assertEquals(E_TAG, response.getHeaders().getETag());
        // The bytes are already gzip in the database and are declared as such; that does not change.
        assertEquals("gzip", response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
    }

    /**
     * The whole point: the same terrain twice costs one round trip instead of four megabytes, and
     * the blob is not even read - which is what makes a revalidation cheap on the server too.
     */
    @Test
    void aBrowserHoldingTheSameHeightMapIsToldSoAndGetsNoBytes() {
        ResponseEntity<byte[]> response = get(E_TAG);

        assertEquals(HttpStatus.NOT_MODIFIED, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(E_TAG, response.getHeaders().getETag());
        verify(planetCrudService, never()).getCompressedHeightMap(anyInt());
    }

    /** The case the no-store header was protecting: a re-uploaded height map has to arrive. */
    @Test
    void aBrowserHoldingAnOlderHeightMapGetsTheNewOne() {
        ResponseEntity<byte[]> response = get("\"an-older-digest\"");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(HEIGHT_MAP, response.getBody());
    }

    @Test
    void theResponseSaysAskBeforeEveryUse() {
        String cacheControl = get(null).getHeaders().getCacheControl();

        assertNotNull(cacheControl);
        assertEquals("no-cache, must-revalidate", cacheControl);
    }

    /**
     * Anything unparsed sends the file. Guessing wrong in this direction costs bandwidth; guessing
     * wrong in the other puts a player on terrain that has been edited away underneath them.
     */
    @Test
    void anythingNotUnderstoodCostsBytesRatherThanFreshness() {
        assertEquals(HttpStatus.OK, get("").getStatusCode());
        assertEquals(HttpStatus.OK, get("garbage").getStatusCode());
        assertEquals(HttpStatus.OK, get(DIGEST).getStatusCode());
    }

    /** A planet with no height map still answers, as it did before there were tags. */
    @Test
    void aPlanetWithoutAHeightMapIsNotTagged() {
        when(planetCrudService.getCompressedHeightMapDigest(7)).thenReturn(null);
        when(planetCrudService.getCompressedHeightMap(7)).thenReturn(null);

        assertEquals(RuntimeException.class,
                org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                        () -> controller.getCompressedHeightMap(7, E_TAG)).getClass());
    }
}
