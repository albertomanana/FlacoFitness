package com.flacofitness.app.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Service
public class BrowserTokenService {

    public static final String COOKIE_NAME = "ff_browser_token";
    public static final String REQUEST_ATTR = "ff.browserToken";

    public String resolveOrCreate(HttpServletRequest request, HttpServletResponse response) {
        Object existingAttr = request.getAttribute(REQUEST_ATTR);
        if (existingAttr instanceof String token && !token.isBlank()) {
            return token;
        }

        String token = readFromCookie(request);
        if (token == null || token.isBlank()) {
            token = UUID.randomUUID().toString();
            writeCookie(response, token);
        }

        request.setAttribute(REQUEST_ATTR, token);
        return token;
    }

    private String readFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    private void writeCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(cookie);
    }
}
