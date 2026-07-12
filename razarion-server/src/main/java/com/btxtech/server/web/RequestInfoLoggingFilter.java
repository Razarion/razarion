package com.btxtech.server.web;

import com.btxtech.server.model.tracking.PageRequest;
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
                pageRequestService.onHome(toPageRequest(httpRequest, queryString));
            } else if ((requestURI.equals("/game") || requestURI.equals("/game/index.html")) && hasQueryString) {
                pageRequestService.onGame(toPageRequest(httpRequest, queryString));
                redditConversionService.sendPageVisitEvent(httpRequest.getParameter("rdt_cid"));
                xConversionService.sendPageVisitEvent(httpRequest.getParameter("twclid"));
            }
        }
        chain.doFilter(request, response);
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
                .rawQueryString(queryString);
    }
}
