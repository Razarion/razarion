package com.btxtech.server.web;

import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.PageRequestType;
import com.btxtech.server.service.tracking.PageRequestService;
import com.btxtech.server.service.tracking.RedditConversionService;
import com.btxtech.server.service.tracking.XConversionService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Every landing page signal comes back through the same pixel URL and is told apart by one query
 * parameter, so that parameter decides whether a visit is counted as a page view, as a press of
 * the play button, or as leaving - and mixing those up would quietly corrupt the funnel rather
 * than fail loudly.
 */
class RequestInfoLoggingFilterTest {
    private final List<PageRequest> saved = new ArrayList<>();
    private final List<PageRequestType> savedTypes = new ArrayList<>();
    private final RequestInfoLoggingFilter filter = new RequestInfoLoggingFilter(
            new PageRequestService(null) {
                @Override
                public void onHomeEvent(PageRequest pageRequest, PageRequestType pageRequestType) {
                    saved.add(pageRequest);
                    savedTypes.add(pageRequestType);
                }

                @Override
                public void onGame(PageRequest pageRequest) {
                    saved.add(pageRequest);
                    savedTypes.add(PageRequestType.GAME);
                }
            },
            mock(RedditConversionService.class),
            mock(XConversionService.class));

    @Test
    void pixelWithoutEventParameterStaysAPageView() throws Exception {
        call("/t.gif", "rdt_cid=abc");

        assertEquals(List.of(PageRequestType.HOME), savedTypes);
        assertEquals("abc", saved.get(0).getRdtCid());
    }

    @Test
    void playEventIsCountedAsPlayClicked() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=play");

        assertEquals(List.of(PageRequestType.HOME_PLAY_CLICKED), savedTypes);
    }

    @Test
    void exitEventCarriesTheDwellTime() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=4200");

        assertEquals(List.of(PageRequestType.HOME_EXIT), savedTypes);
        assertEquals(4200, saved.get(0).getDwellMillis());
    }

    /**
     * The whole URL is craftable, so an implausible duration is dropped rather than stored - one
     * forged value would otherwise be enough to move an average.
     */
    @Test
    void implausibleDwellTimeIsDropped() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=999999999");

        assertNull(saved.get(0).getDwellMillis());
    }

    @Test
    void nonNumericDwellTimeIsDropped() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=soon");

        assertNull(saved.get(0).getDwellMillis());
    }

    /**
     * Counting an unknown event as a page view keeps a future or garbled parameter from making
     * the visit disappear altogether.
     */
    @Test
    void unknownEventFallsBackToPageView() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=something");

        assertEquals(List.of(PageRequestType.HOME), savedTypes);
    }

    /**
     * Telling a browser from a crawler is the reason this was added: a landing page visit that
     * never continues looks the same either way without it.
     */
    @Test
    void userAgentAndRefererAreRecorded() throws Exception {
        MockHttpServletRequest request = request("/t.gif", "rdt_cid=abc");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0)");
        request.addHeader("Referer", "https://www.reddit.com/");

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertEquals("Mozilla/5.0 (Windows NT 10.0)", saved.get(0).getUserAgent());
        assertEquals("https://www.reddit.com/", saved.get(0).getReferer());
    }

    @Test
    void overlongUserAgentIsCutToLength() throws Exception {
        MockHttpServletRequest request = request("/t.gif", "rdt_cid=abc");
        request.addHeader("User-Agent", "x".repeat(2000));

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertEquals(512, saved.get(0).getUserAgent().length());
    }

    /**
     * The event parameter belongs to the landing page; the game page keeps being counted as the
     * game page whatever the query string carries over from it.
     */
    @Test
    void gamePageIsUnaffectedByTheEventParameter() throws Exception {
        call("/game", "rdt_cid=abc&e=play");

        assertEquals(List.of(PageRequestType.GAME), savedTypes);
    }

    /**
     * A request without a query string carries no campaign at all and was never tracked - the
     * events must not change that, or home and game would count different populations.
     */
    @Test
    void requestWithoutQueryStringIsNotTracked() throws Exception {
        call("/t.gif", null);

        assertEquals(List.of(), savedTypes);
    }

    private void call(String uri, String queryString) throws Exception {
        filter.doFilter(request(uri, queryString), new MockHttpServletResponse(), mock(FilterChain.class));
    }

    private MockHttpServletRequest request(String uri, String queryString) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        if (queryString != null) {
            request.setQueryString(queryString);
            for (String pair : queryString.split("&")) {
                String[] keyValue = pair.split("=", 2);
                request.setParameter(keyValue[0], keyValue.length > 1 ? keyValue[1] : "");
            }
        }
        return request;
    }
}
