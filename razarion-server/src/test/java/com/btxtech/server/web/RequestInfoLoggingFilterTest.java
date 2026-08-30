package com.btxtech.server.web;

import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.PageRequestType;
import com.btxtech.server.service.tracking.MetaConversionService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Every landing page signal comes back through the same pixel URL and is told apart by one query
 * parameter, so that parameter decides whether a visit is counted as a page view, as a press of
 * the play button, or as leaving - and mixing those up would quietly corrupt the funnel rather
 * than fail loudly.
 */
class RequestInfoLoggingFilterTest {
    private final MetaConversionService metaConversionService = mock(MetaConversionService.class);
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

                @Override
                public void onLanding(PageRequest pageRequest) {
                    saved.add(pageRequest);
                    savedTypes.add(PageRequestType.LANDING);
                }
            },
            mock(RedditConversionService.class),
            mock(XConversionService.class),
            metaConversionService);

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
     * The duration alone cannot say why a visit was short. These fields are what separates a page
     * nobody ever saw from one that was looked at and turned down, so an exit has to carry all of
     * them through.
     */
    @Test
    void exitEventCarriesWhatHappenedDuringTheVisit() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=4200&r=h&v=1&i=1&l=850&fp=1100&hb=2300");

        PageRequest pageRequest = saved.get(0);
        assertEquals("hidden", pageRequest.getExitReason());
        assertEquals(Boolean.TRUE, pageRequest.getVisibleAtStart());
        assertEquals(Boolean.TRUE, pageRequest.getInteracted());
        assertEquals(Boolean.FALSE, pageRequest.getPrerendered());
        assertEquals(850, pageRequest.getLoadMillis());
        assertEquals(1100, pageRequest.getFirstPaintMillis());
        assertEquals(2300, pageRequest.getHeroLoadedMillis());
    }

    /**
     * A visit that arrived behind the app it came from, painted nothing and was never touched -
     * the case this was built to find. What is absent has to stay absent: a hero time of zero
     * would read as "arrived instantly" instead of "never arrived".
     */
    @Test
    void exitOfAVisitNobodySawIsRecordedAsSuch() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=180&r=u&v=0");

        PageRequest pageRequest = saved.get(0);
        assertEquals("pagehide", pageRequest.getExitReason());
        assertEquals(Boolean.FALSE, pageRequest.getVisibleAtStart());
        assertEquals(Boolean.FALSE, pageRequest.getInteracted());
        assertNull(pageRequest.getFirstPaintMillis());
        assertNull(pageRequest.getHeroLoadedMillis());
    }

    /**
     * Only the exit event reports these. On a page view "not interacted" would be a statement
     * about a visit that is still running, which is no statement at all.
     */
    @Test
    void pageViewMakesNoClaimAboutWhatHappened() throws Exception {
        call("/t.gif", "rdt_cid=abc");

        PageRequest pageRequest = saved.get(0);
        assertNull(pageRequest.getInteracted());
        assertNull(pageRequest.getPrerendered());
        assertNull(pageRequest.getVisibleAtStart());
        assertNull(pageRequest.getExitReason());
    }

    @Test
    void implausibleOrGarbledMeasurementsAreDropped() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=4200&l=-5&fp=soon&hb=999999999&r=sideways");

        PageRequest pageRequest = saved.get(0);
        assertNull(pageRequest.getLoadMillis());
        assertNull(pageRequest.getFirstPaintMillis());
        assertNull(pageRequest.getHeroLoadedMillis());
        assertNull(pageRequest.getExitReason());
        assertEquals(4200, pageRequest.getDwellMillis());
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

    /**
     * The landing page request is the only one that ever sees where a visitor came from: the pixel
     * is a subresource of this page and the game is the page after it, so both report razarion.com.
     */
    @Test
    void landingPageIsRecordedForItsReferer() throws Exception {
        MockHttpServletRequest request = request("/", null);
        request.addHeader("Referer", "https://x.com/AloRtsDev/status/2084534615864496500");

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertEquals(List.of(PageRequestType.LANDING), savedTypes);
        assertEquals("https://x.com/AloRtsDev/status/2084534615864496500", saved.get(0).getReferer());
    }

    /**
     * Campaign visitors are recorded whether or not a referrer arrived - the click id is the point
     * for them, and it ties the visit to the game session that follows.
     */
    @Test
    void landingPageWithCampaignButNoRefererIsRecorded() throws Exception {
        call("/", "twclid=abc");

        assertEquals(List.of(PageRequestType.LANDING), savedTypes);
        assertEquals("abc", saved.get(0).getTwclid());
    }

    /**
     * Why a visitor did not press Play, which is the one thing the numbers could not say: 98 of
     * 100 paid arrivals leave without pressing it, and "never saw the button", "saw it and
     * declined" and "pressed it and nothing happened" were the same row until now.
     */
    @Test
    void theExitSaysWhatBecameOfTheButton() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=6100&bs=1400&bp=1&sd=35&vp=412x780");

        PageRequest pageRequest = saved.get(0);
        assertEquals(1400, pageRequest.getButtonSeenMillis());
        assertEquals(Boolean.TRUE, pageRequest.getButtonPressed());
        assertEquals(35, pageRequest.getScrollDepth());
        assertEquals("412x780", pageRequest.getViewport());
    }

    /**
     * A button that was never on screen reports nothing rather than zero: never shown and shown
     * at once are opposite findings, and zero would read as the second.
     */
    @Test
    void aButtonThatWasNeverOnScreenIsAbsentRatherThanZero() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=900");

        PageRequest pageRequest = saved.get(0);
        assertNull(pageRequest.getButtonSeenMillis());
        assertNull(pageRequest.getScrollDepth());
        assertNull(pageRequest.getViewport());
        // Not null: on an exit, no press reported is a statement that none happened - unlike the
        // three above, where absent means the measurement itself never came.
        assertEquals(Boolean.FALSE, pageRequest.getButtonPressed());
    }

    /**
     * The four ways a touch on the button dies are four different repairs - a webview that steals
     * the gesture is not a slop limit set too tight - so the letter and its measure are kept apart
     * and kept whole.
     */
    @Test
    void theExitSaysHowTheGestureDied() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=6100&bp=1&tf=d&tm=47");

        PageRequest pageRequest = saved.get(0);
        assertEquals(Boolean.TRUE, pageRequest.getButtonPressed());
        assertEquals("d", pageRequest.getTapFailure());
        assertEquals(47, pageRequest.getTapFailureMeasure());
    }

    /** A touch that became a game reports no failure at all - and neither does a visit without one. */
    @Test
    void aGestureThatDidNotFailReportsNothing() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=900&bp=1");

        PageRequest pageRequest = saved.get(0);
        assertNull(pageRequest.getTapFailure());
        assertNull(pageRequest.getTapFailureMeasure());
    }

    /**
     * These are counted by category. A letter the page never sends would become a category of its
     * own in every report from then on, so it is dropped rather than stored.
     */
    @Test
    void anInventedFailureLetterIsDropped() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=900&bp=1&tf=x&tm=12");

        assertNull(saved.get(0).getTapFailure());
        // The measure alone says nothing without the letter, but it is bounded and harmless; what
        // matters is that no report can be made to grow a category it was never meant to have.
        assertEquals(12, saved.get(0).getTapFailureMeasure());
    }

    /** Everything here rides on a url anyone can craft, so nothing implausible is stored. */
    @Test
    void craftedButtonMeasurementsAreDropped() throws Exception {
        call("/t.gif", "rdt_cid=abc&e=exit&d=900&sd=4000&vp=<script>&bs=soon&tf=dragged&tm=-5");

        PageRequest pageRequest = saved.get(0);
        assertNull(pageRequest.getScrollDepth());
        assertNull(pageRequest.getViewport());
        assertNull(pageRequest.getButtonSeenMillis());
        assertNull(pageRequest.getTapFailure());
        assertNull(pageRequest.getTapFailureMeasure());
    }

    /**
     * Meta is told about the landing page because the step below it, the game page, is reached by
     * barely one visitor in a hundred - too rarely for its optimiser to learn anything. All three
     * landing signals ride on the same pixel url, so reporting the wrong ones would count one
     * visitor three times and quietly inflate the very number the campaign is steered by.
     */
    @Test
    void onlyTheLandingViewItselfIsReportedToMeta() throws Exception {
        call("/t.gif", "fbclid=abc");
        call("/t.gif", "fbclid=abc&e=play");
        call("/t.gif", "fbclid=abc&e=exit&d=1000");

        assertEquals(List.of(PageRequestType.HOME, PageRequestType.HOME_PLAY_CLICKED,
                PageRequestType.HOME_EXIT), savedTypes);
        verify(metaConversionService, times(1)).sendLandingViewEvent(eq("abc"), any());
    }

    /** A visitor who carries no Meta click id is nobody Meta can be told about. */
    @Test
    void aLandingViewWithoutAMetaClickIdReportsAnEmptyClickId() throws Exception {
        call("/t.gif", "rdt_cid=abc");

        verify(metaConversionService, times(1)).sendLandingViewEvent(eq(null), any());
        verify(metaConversionService, never()).sendPageVisitEvent(any(), any());
    }

    /**
     * Meta's click id arrives on its own from some placements - no utm source, no referrer. Before
     * it had a field of its own, such a visit was stored with nothing but the raw query string and
     * was reported as organic.
     */
    @Test
    void metaClickIdIsStored() throws Exception {
        call("/", "fbclid=IwcGRvZgRle");

        assertEquals(List.of(PageRequestType.LANDING), savedTypes);
        assertEquals("IwcGRvZgRle", saved.get(0).getFbclid());
    }

    /**
     * Nothing to say, no row: "/" is what every crawler asks for first, and a request with neither
     * a referrer nor a campaign carries nothing that is not already known.
     */
    @Test
    void landingPageWithoutRefererOrCampaignIsNotRecorded() throws Exception {
        call("/", null);

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
