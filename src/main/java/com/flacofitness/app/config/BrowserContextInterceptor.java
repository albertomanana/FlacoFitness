package com.flacofitness.app.config;

import com.flacofitness.app.service.BrowserTokenService;
import com.flacofitness.app.service.RecentVisitService;
import com.flacofitness.app.service.RequestContextService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class BrowserContextInterceptor implements HandlerInterceptor {

    private final BrowserTokenService browserTokenService;
    private final RecentVisitService recentVisitService;
    private final RequestContextService requestContextService;

    public BrowserContextInterceptor(BrowserTokenService browserTokenService,
                                     RecentVisitService recentVisitService,
                                     RequestContextService requestContextService) {
        this.browserTokenService = browserTokenService;
        this.recentVisitService = recentVisitService;
        this.requestContextService = requestContextService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        browserTokenService.resolveOrCreate(request, response);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        if (ex != null || response.getStatus() >= 400) {
            return;
        }

        recentVisitService.registerIfTrackable(
                request,
                requestContextService.resolveProfile(request),
                browserTokenService.resolveOrCreate(request, response));
    }
}
