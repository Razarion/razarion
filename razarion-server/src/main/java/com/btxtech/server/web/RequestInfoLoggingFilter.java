package com.btxtech.server.web;

import com.btxtech.server.model.tracking.PageRequest;
import com.btxtech.server.model.tracking.PageRequestType;
import com.btxtech.server.service.tracking.MetaConversionService;
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
     * The rest of what the exit event carries. Short names because they ride on a URL that already
     * carries the visitor's whole campaign query string; what each one means is documented on the
     * field it fills in {@link PageRequest}.
     */
    private static final String VISIBLE_AT_START_PARAMETER = "v";
    private static final String PRERENDERED_PARAMETER = "pr";
    private static final String LOAD_MILLIS_PARAMETER = "l";
    private static final String FIRST_PAINT_MILLIS_PARAMETER = "fp";
    private static final String HERO_MILLIS_PARAMETER = "hb";
    private static final String INTERACTED_PARAMETER = "i";
    private static final String EXIT_REASON_PARAMETER = "r";
    /**
     * The four that say why a visitor did not press Play. What each one means is documented on the
     * field it fills in {@link PageRequest}.
     */
    private static final String BUTTON_SEEN_PARAMETER = "bs";
    private static final String BUTTON_PRESSED_PARAMETER = "bp";
    private static final String SCROLL_DEPTH_PARAMETER = "sd";
    private static final String VIEWPORT_PARAMETER = "vp";
    private static final String EXIT_REASON_HIDDEN = "h";
    private static final String EXIT_REASON_PAGEHIDE = "u";
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
    private final MetaConversionService metaConversionService;

    public RequestInfoLoggingFilter(PageRequestService pageRequestService,
                                    RedditConversionService redditConversionService,
                                    XConversionService xConversionService,
                                    MetaConversionService metaConversionService) {
        this.pageRequestService = pageRequestService;
        this.redditConversionService = redditConversionService;
        this.xConversionService = xConversionService;
        this.metaConversionService = metaConversionService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            var hasQueryString = queryString != null && !queryString.isEmpty();

            if (requestURI.equals("/") && isLandingWorthRecording(httpRequest, hasQueryString)) {
                pageRequestService.onLanding(toPageRequest(httpRequest, queryString));
            } else if (requestURI.equals("/t.gif") && hasQueryString) {
                PageRequestType homeEventType = homeEventType(httpRequest);
                pageRequestService.onHomeEvent(toPageRequest(httpRequest, queryString), homeEventType);
                if (homeEventType == PageRequestType.HOME) {
                    // Only the page view, not the click and not the exit: those ride on the same
                    // pixel url and would report one visitor three times. Meta is told about this
                    // step at all because the one below it - the game page - is reached too rarely
                    // to optimise on; see MetaConversionService.
                    metaConversionService.sendLandingViewEvent(httpRequest.getParameter("fbclid"),
                            httpRequest.getHeader("User-Agent"));
                }
            } else if ((requestURI.equals("/game") || requestURI.equals("/game/index.html")) && hasQueryString) {
                pageRequestService.onGame(toPageRequest(httpRequest, queryString));
                redditConversionService.sendPageVisitEvent(httpRequest.getParameter("rdt_cid"));
                xConversionService.sendPageVisitEvent(httpRequest.getParameter("twclid"));
                metaConversionService.sendPageVisitEvent(httpRequest.getParameter("fbclid"),
                        httpRequest.getHeader("User-Agent"));
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * Whether the landing page request has anything to say. It is recorded for its Referer header,
     * so a request without one and without campaign parameters carries nothing that is not already
     * known - and "/" is also what every crawler asks for first, which is not worth a row each.
     * <p>
     * Only GET: this is a page view, and it must never be a POST that happens to land here.
     */
    private boolean isLandingWorthRecording(HttpServletRequest httpRequest, boolean hasQueryString) {
        return "GET".equals(httpRequest.getMethod())
                && (hasQueryString || httpRequest.getHeader("Referer") != null);
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
                .fbclid(httpRequest.getParameter("fbclid"))
                .userAgent(trim(httpRequest.getHeader("User-Agent")))
                .referer(trim(httpRequest.getHeader("Referer")))
                .dwellMillis(millis(httpRequest, DWELL_PARAMETER))
                .visibleAtStart(flag(httpRequest, VISIBLE_AT_START_PARAMETER))
                .prerendered(presentFlag(httpRequest, PRERENDERED_PARAMETER))
                .loadMillis(millis(httpRequest, LOAD_MILLIS_PARAMETER))
                .firstPaintMillis(millis(httpRequest, FIRST_PAINT_MILLIS_PARAMETER))
                .heroLoadedMillis(millis(httpRequest, HERO_MILLIS_PARAMETER))
                .interacted(presentFlag(httpRequest, INTERACTED_PARAMETER))
                .buttonSeenMillis(millis(httpRequest, BUTTON_SEEN_PARAMETER))
                .buttonPressed(presentFlag(httpRequest, BUTTON_PRESSED_PARAMETER))
                .scrollDepth(percent(httpRequest, SCROLL_DEPTH_PARAMETER))
                .viewport(viewport(httpRequest))
                .exitReason(exitReason(httpRequest))
                .rawQueryString(queryString);
    }

    /**
     * A duration reported by the page. Everything here comes from a URL anyone can craft, so a
     * value that is not a plain number inside a plausible range is dropped rather than stored -
     * one manipulated visit must not be able to move a median.
     */
    private Integer millis(HttpServletRequest httpRequest, String parameter) {
        String value = httpRequest.getParameter(parameter);
        if (value == null) {
            return null;
        }
        try {
            int millis = Integer.parseInt(value);
            return millis >= 0 && millis <= MAX_DWELL_MILLIS ? millis : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * A percentage reported by the page. Same reasoning as {@link #millis}: it comes from a url
     * anyone can craft, so anything outside nought to a hundred is dropped rather than stored.
     */
    private Integer percent(HttpServletRequest httpRequest, String parameter) {
        String value = httpRequest.getParameter(parameter);
        if (value == null) {
            return null;
        }
        try {
            int percent = Integer.parseInt(value);
            return percent >= 0 && percent <= 100 ? percent : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * The viewport, only in the shape the page sends it: two numbers with an x between them. It is
     * shown in a report next to a user agent, and an attacker-controlled string that is neither
     * bounded nor checked has no business being stored just because it arrived.
     */
    private String viewport(HttpServletRequest httpRequest) {
        String value = httpRequest.getParameter(VIEWPORT_PARAMETER);
        return value != null && value.matches("\\d{1,5}x\\d{1,5}") ? value : null;
    }

    /** A parameter sent as "1" or "0". Absent stays absent - it is not the same as false. */
    private Boolean flag(HttpServletRequest httpRequest, String parameter) {
        String value = httpRequest.getParameter(parameter);
        if (value == null) {
            return null;
        }
        return "1".equals(value) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * A parameter the page only appends when it is true, to keep the URL short. Absent therefore
     * means false rather than unknown - but only on an exit event, which is the only thing that
     * sends these at all.
     */
    private Boolean presentFlag(HttpServletRequest httpRequest, String parameter) {
        if (!EVENT_EXIT.equals(httpRequest.getParameter(EVENT_PARAMETER))) {
            return null;
        }
        return "1".equals(httpRequest.getParameter(parameter));
    }

    private String exitReason(HttpServletRequest httpRequest) {
        String reason = httpRequest.getParameter(EXIT_REASON_PARAMETER);
        if (EXIT_REASON_HIDDEN.equals(reason)) {
            return "hidden";
        }
        if (EXIT_REASON_PAGEHIDE.equals(reason)) {
            return "pagehide";
        }
        return null;
    }

    private String trim(String header) {
        if (header == null) {
            return null;
        }
        return header.length() <= MAX_HEADER_LENGTH ? header : header.substring(0, MAX_HEADER_LENGTH);
    }
}
