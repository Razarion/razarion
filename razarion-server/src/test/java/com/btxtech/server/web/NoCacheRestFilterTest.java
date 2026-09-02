package com.btxtech.server.web;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * The filter exists to stop a cache in front of this pod answering for it. The model file is the
 * one response that must escape it - eleven megabytes on every game start - and it escapes only in
 * the exact shape that keeps the same guarantee: a read, revalidated. Getting that boundary wrong
 * in either direction is expensive: too wide and a write is served from a cache, too narrow and
 * every start pays the download again.
 */
class NoCacheRestFilterTest {
    private final NoCacheRestFilter filter = new NoCacheRestFilter();

    private MockHttpServletResponse call(String method, String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRequestURI(uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, mock(FilterChain.class));
        return response;
    }

    @Test
    void readingTheModelIsLeftToTheControllerToDeclare() throws Exception {
        MockHttpServletResponse response = call("GET", "/rest/gltf/glb/1");

        // Not merely a different Cache-Control: the filter also sets Pragma and Expires, and a
        // controller declaring its own Cache-Control would not clear those.
        assertNull(response.getHeader("Cache-Control"));
        assertNull(response.getHeader("Pragma"));
        assertNull(response.getHeader("Expires"));
    }

    /**
     * The terrain height map is the second one: four megabytes that the game engine worker waits
     * for before the game can run, and it declares its own entity tag exactly as the model does.
     */
    @Test
    void readingTheHeightMapIsAlsoLeftToItsController() throws Exception {
        MockHttpServletResponse response = call("GET", "/rest/terrainHeightMap/117");

        assertNull(response.getHeader("Cache-Control"));
        assertNull(response.getHeader("Pragma"));
        assertNull(response.getHeader("Expires"));
    }

    /**
     * The materials are the third and were the largest: six responses, eight megabytes, 51% of
     * everything a player downloads before the game is playable. They were re-sent in full on
     * every start until STARTUP_PAYLOAD weighed them.
     */
    @Test
    void readingAMaterialIsAlsoLeftToItsController() throws Exception {
        MockHttpServletResponse response = call("GET", "/rest/babylon-material/data/2");

        assertNull(response.getHeader("Cache-Control"));
        assertNull(response.getHeader("Pragma"));
        assertNull(response.getHeader("Expires"));
    }

    /** The upload sits one path segment away from the read and must not inherit its freedom. */
    @Test
    void uploadingAMaterialStaysUnderTheBlanketRule() throws Exception {
        assertEquals("no-store, no-cache, must-revalidate, max-age=0",
                call("POST", "/rest/babylon-material/upload/2").getHeader("Cache-Control"));
        // The size listing is not the blob and has no tag of its own.
        assertEquals("no-store, no-cache, must-revalidate, max-age=0",
                call("GET", "/rest/babylon-material/sizes").getHeader("Cache-Control"));
    }

    /**
     * The particle systems are the fourth. They sit under /rest/editor/ although every player
     * reads them, which is exactly why nobody went looking for them under a cache rule - the path
     * says editor, the traffic says game.
     */
    @Test
    void readingAParticleSystemIsAlsoLeftToItsController() throws Exception {
        MockHttpServletResponse response = call("GET", "/rest/editor/particle-system/data/6");

        assertNull(response.getHeader("Cache-Control"));
        assertNull(response.getHeader("Pragma"));
        assertNull(response.getHeader("Expires"));
    }

    /**
     * Everything else under /rest/editor/ keeps the blanket rule. The exemption is one path that
     * happens to live there, not a hole in the editor.
     */
    @Test
    void theRestOfTheEditorStaysUnderTheBlanketRule() throws Exception {
        assertEquals("no-store, no-cache, must-revalidate, max-age=0",
                call("GET", "/rest/editor/particle-system/sizes").getHeader("Cache-Control"));
        assertEquals("no-store, no-cache, must-revalidate, max-age=0",
                call("PUT", "/rest/editor/particle-system/upload/6").getHeader("Cache-Control"));
        assertEquals("no-store, no-cache, must-revalidate, max-age=0",
                call("GET", "/rest/editor/base-item-type/1").getHeader("Cache-Control"));
    }

    /**
     * The terrain shape beside it is NOT exempt. It is computed per pod at startup rather than
     * stored, so no digest computed here can be trusted to match another pod's - the same argument
     * that put the model's digest in the database.
     */
    @Test
    void theTerrainShapeStaysUnderTheBlanketRule() throws Exception {
        assertEquals("no-store, no-cache, must-revalidate, max-age=0",
                call("GET", "/rest/terrainshape/117").getHeader("Cache-Control"));
    }

    /** A write is the case the filter was added for, and it sits next to the read it must not follow. */
    @Test
    void uploadingAModelStaysUnderTheBlanketRule() throws Exception {
        MockHttpServletResponse response = call("PUT", "/rest/gltf/upload-glb/1");

        assertEquals("no-store, no-cache, must-revalidate, max-age=0", response.getHeader("Cache-Control"));
    }

    /**
     * The exemption is for reading, not for the path. A POST to the same URL is not a model read,
     * whatever it is, and must not inherit the read's freedom.
     */
    @Test
    void onlyAGetOnThatPathIsExempt() throws Exception {
        assertTrue(call("POST", "/rest/gltf/glb/1").getHeader("Cache-Control").startsWith("no-store"));
    }

    @Test
    void everyOtherRestPathIsUntouchedByThisChange() throws Exception {
        assertEquals("no-store, no-cache, must-revalidate, max-age=0",
                call("GET", "/rest/gltf/1").getHeader("Cache-Control"));
        assertEquals("no-store, no-cache, must-revalidate, max-age=0",
                call("GET", "/rest/tracking/startup-task").getHeader("Cache-Control"));
        assertEquals("no-store, no-cache, must-revalidate, max-age=0",
                call("GET", "/editor/anything").getHeader("Cache-Control"));
    }

    /** Static assets never went through this and must keep their own long-lived headers. */
    @Test
    void staticAssetsAreNotThisFiltersBusiness() throws Exception {
        assertNull(call("GET", "/game/main-ABC123.js").getHeader("Cache-Control"));
    }
}
