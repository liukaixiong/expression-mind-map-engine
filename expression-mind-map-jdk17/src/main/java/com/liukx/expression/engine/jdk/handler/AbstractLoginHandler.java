package com.liukx.expression.engine.jdk.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;


@Component
public abstract class AbstractLoginHandler implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(AbstractLoginHandler.class);

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
}
