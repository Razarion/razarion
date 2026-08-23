package com.btxtech.server.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The landing page is built per visitor: their query string goes into the tracking pixel and into
 * the "Play Now" link. A cached copy therefore reports the wrong visitor, or none - which is what
 * a CDN in front of production did while the page went out with no cache header at all.
 */
class IndexControllerTest {
    private final IndexController indexController = new IndexController();

    @Test
    void landingPageIsNeverStored() {
        MockHttpServletResponse response = render("twclid=abc");

        String cacheControl = response.getHeader(HttpHeaders.CACHE_CONTROL);
        assertNotNull(cacheControl, "the landing page must say it is not to be stored");
        assertTrue(cacheControl.contains("no-store"), "was: " + cacheControl);
    }

    /**
     * The header is about the page being visitor specific, and a visitor without campaign
     * parameters is still a visitor - the pixel and the link are rendered either way.
     */
    @Test
    void landingPageWithoutQueryStringIsNotStoredEither() {
        MockHttpServletResponse response = render(null);

        assertTrue(response.getHeader(HttpHeaders.CACHE_CONTROL).contains("no-store"));
    }

    @Test
    void queryStringReachesTheTemplate() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setQueryString("twclid=abc&utm_source=twitter");
        Model model = new ExtendedModelMap();

        assertEquals("index", indexController.index(request, new MockHttpServletResponse(), model));
        assertEquals("?twclid=abc&utm_source=twitter", model.getAttribute("qs"));
    }

    private MockHttpServletResponse render(String queryString) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        request.setQueryString(queryString);
        MockHttpServletResponse response = new MockHttpServletResponse();

        indexController.index(request, response, new ExtendedModelMap());

        return response;
    }
}
