package com.btxtech.server.web;

import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.PageRequestType;
import com.btxtech.server.service.tracking.PageRequestService;
import com.btxtech.server.service.tracking.RedditConversionService;
import com.btxtech.server.service.tracking.XConversionService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestInfoLoggingFilter implements Filter {
    /**
     * Query parameter the landing page adds to the pixel URL to say which of its events this is.
     * Absent means the plain page view, which is how the pixel behaved before there were events.
     */
    private static final String EVENT_PARAMETER = "e";
    private static final String EVENT_PLAY_CLICKED = "play";
    private static final String EVENT_EXIT = "exit";
    /** Milliseconds the landing page was open, sent along with the exit event. */
    private static final String DWELL_PARAMETER = "d";
    /**
     * A user agent is worth storing to tell browsers and bots apart, but it is attacker-controlled
     * and unbounded, so it does not go into the database at arbitrary length.
     */
    private static final int MAX_HEADER_LENGTH = 512;
    /**
     * Anything longer than this is not a page visit any more. Keeps a manipulated dwell time from
     * dragging an average into nonsense.
     */
    private static final int MAX_DWELL_MILLIS = 6 * 60 * 60 * 1000;

    private final PageRequestService pageRequestService;
    private final RedditConversionService redditConversionService;
    private final XConversionService xConversionService;

    public RequestInfoLoggingFilter(PageRequestService pageRequestService,
                                    RedditConversionService redditConversionService,
                                    XConversionService xConversionService) {
        this.pageRequestService = pageRequestService;
        this.redditConversionService = redditConversionService;
        this.xConversionService = xConversionService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            var hasQueryString = queryString != null && !queryString.isEmpty();

            if (requestURI.equals("/t.gif") && hasQueryString) {
                pageRequestService.onHomeEvent(toPageRequest(httpRequest, queryString),
                        homeEventType(httpRequest));
            } else if ((requestURI.equals("/game") || requestURI.equals("/game/index.html")) && hasQueryString) {
                pageRequestService.onGame(toPageRequest(httpRequest, queryString));
                redditConversionService.sendPageVisitEvent(httpRequest.getParameter("rdt_cid"));
                xConversionService.sendPageVisitEvent(httpRequest.getParameter("twclid"));
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * An unknown event value is treated as the plain page view. The parameter comes from a URL
     * anyone can craft, and a visit counted twice is a smaller problem than a request dropped.
     */
    private PageRequestType homeEventType(HttpServletRequest httpRequest) {
        String event = httpRequest.getParameter(EVENT_PARAMETER);
        if (EVENT_PLAY_CLICKED.equals(event)) {
            return PageRequestType.HOME_PLAY_CLICKED;
        }
        if (EVENT_EXIT.equals(event)) {
            return PageRequestType.HOME_EXIT;
        }
        return PageRequestType.HOME;
    }

    private PageRequest toPageRequest(HttpServletRequest httpRequest, String queryString) {
        HttpSession session = httpRequest.getSession(true);
        String sessionId = session != null ? session.getId() : null;
        return new PageRequest()
                .httpSessionId(sessionId)
                .utmCampaign(httpRequest.getParameter("utm_campaign"))
                .utmSource(httpRequest.getParameter("utm_source"))
                .utmMedium(httpRequest.getParameter("utm_medium"))
                .rdtCid(httpRequest.getParameter("rdt_cid"))
                .twclid(httpRequest.getParameter("twclid"))
                .userAgent(trim(httpRequest.getHeader("User-Agent")))
                .referer(trim(httpRequest.getHeader("Referer")))
                .dwellMillis(dwellMillis(httpRequest))
                .rawQueryString(queryString);
    }

    private Integer dwellMillis(HttpServletRequest httpRequest) {
        String dwell = httpRequest.getParameter(DWELL_PARAMETER);
        if (dwell == null) {
            return null;
        }
        try {
            int millis = Integer.parseInt(dwell);
            return millis >= 0 && millis <= MAX_DWELL_MILLIS ? millis : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trim(String header) {
        if (header == null) {
            return null;
        }
        return header.length() <= MAX_HEADER_LENGTH ? header : header.substring(0, MAX_HEADER_LENGTH);
    }
}
