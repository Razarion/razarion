package com.btxtech.server;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.transaction.Transactional;
import java.io.IOException;

/**
 * Created by Beat
 * 07.02.2016.
 */
@WebFilter(urlPatterns = "*")
public class TransactionPerViewFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Ignore
    }

    @Override
    @Transactional
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        // Ignore
    }
}
