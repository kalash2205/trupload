package com.mathworks.bat.trupload.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;

import com.mathworks.bat.common.reqctx.HeaderNames;
import com.mathworks.bat.common.reqctx.RequestContext;

/**
 * This class does four things:
 *
 * <pre>
 * 1) Adds requestId (uuId) to each
 * 2) Sets requestId to log4j2 thread context
 * 3) Sets requestId and origin from request header to request context.
 * </pre>
 */

public class RequestFilter implements Filter {
    public static final String FORWARDED_FOR_HEADER_NAME = "X-Forwarded-For";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initial configuration.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        try {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            final RequestContext requestContext = RequestContext.set(
                httpRequest.getHeader(HeaderNames.REQUEST_ID),
                getOriginOrDefault(httpRequest)
            );

            // For logging
            ThreadContext.put(RequestContext.REQUEST_ID, requestContext.getRequestId());

            chain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }

    @Override
    public void destroy() {
        ThreadContext.clearAll();
    }

    /**
     * Gets the origin from the header; if not found, gets the host-name (or IP) of the client as
     * default origin.
     * @param httpRequest Http servlet request
     * @return Origin from the header, or the client's host-name (or IP)
     */
    private String getOriginOrDefault(final HttpServletRequest httpRequest) {
        String result = httpRequest.getHeader(HeaderNames.ORIGIN);
        if (StringUtils.isBlank(result)) {
            result = httpRequest.getHeader(FORWARDED_FOR_HEADER_NAME);
            if (StringUtils.isBlank(result)) {
                result = httpRequest.getRemoteHost();
            }
        }
        return result;
    }
}