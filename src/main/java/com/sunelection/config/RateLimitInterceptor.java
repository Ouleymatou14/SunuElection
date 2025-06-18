package com.sunelection.config;

import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> rateLimiters;

    public RateLimitInterceptor(Map<String, Bucket> rateLimiters) {
        this.rateLimiters = rateLimiters;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        Bucket bucket;

        // Sélectionner le bucket approprié en fonction du chemin
        if (path.startsWith("/api/auth")) {
            bucket = rateLimiters.get("auth");
        } else {
            bucket = rateLimiters.get("api");
        }

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            return false;
        }
    }
} 