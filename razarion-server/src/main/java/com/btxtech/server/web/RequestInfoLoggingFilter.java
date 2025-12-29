package com.btxtech.server.web;

import com.btxtech.server.service.tracking.PageRequestService;
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

    public RequestInfoLoggingFilter(PageRequestService pageRequestService) {
        this.pageRequestService = pageRequestService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            var hasQueryString = queryString != null && !queryString.isEmpty();

            if (requestURI.equals("/styles.css") && hasQueryString) {
                HttpSession session = httpRequest.getSession(true);
                String sessionId = session != null ? session.getId() : null;
                String rdtCid = httpRequest.getParameter("rdt_cid");
                String utmCampaign = httpRequest.getParameter("utm_campaign");
                String utmSource = httpRequest.getParameter("utm_source");
                pageRequestService.onHome(sessionId, utmCampaign, utmSource, rdtCid);
            } else if ((requestURI.equals("/game") || requestURI.equals("/game/index.html")) && hasQueryString) {
                HttpSession session = httpRequest.getSession(true);
                String sessionId = session != null ? session.getId() : null;
                String rdtCid = httpRequest.getParameter("rdt_cid");
                String utmCampaign = httpRequest.getParameter("utm_campaign");
                String utmSource = httpRequest.getParameter("utm_source");
                pageRequestService.onGame(sessionId, utmCampaign, utmSource, rdtCid);
            }
        }
        chain.doFilter(request, response);
    }
}
