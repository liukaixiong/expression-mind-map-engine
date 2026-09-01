package com.liukx.expression.engine.jdk.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public abstract class AbstractLoginHandler implements HandlerInterceptor {

    public abstract boolean preHandle0(Object request, Object response, Object handler)
            throws Exception;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        return preHandle0(request, response, handler);
    }

    protected void sendRedirect(Object response, String path) throws IOException {
        ((HttpServletResponse) response).sendRedirect(path);
    }

    protected void addHeader(Object response, String headerName, String headerValue) {
        ((HttpServletResponse) response).setHeader(headerName, headerValue);
    }

    protected String getRequestURI(Object request) {
        return ((HttpServletRequest) request).getRequestURI();
    }
}
