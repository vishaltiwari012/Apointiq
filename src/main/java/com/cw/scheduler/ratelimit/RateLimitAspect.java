package com.cw.scheduler.ratelimit;

import com.cw.scheduler.exception.RateLimitExceededException;
import com.cw.scheduler.security.AuthenticationFacade;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiterService rateLimiterService;
    private final AuthenticationFacade authenticationFacade;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {

        // Prefer authenticated user's ID, fallback to session ID for guests
        String userKey = getUserKey();

        Bucket bucket = rateLimiterService.resolveBucket(
                userKey,
                rateLimit.capacity(),
                rateLimit.refillTokens(),
                rateLimit.refillDurationSeconds()
        );

        if (bucket.tryConsume(1)) {
            return pjp.proceed();
        }

        throw new RateLimitExceededException(rateLimit.message());
    }

    private String getUserKey() {
        try {
            Long userId = authenticationFacade.getCurrentUserId();
            return "USER_" + userId;
        } catch (Exception e) {
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip != null && !ip.isEmpty()) {
                    ip = ip.split(",")[0].trim();
                } else {
                    ip = request.getRemoteAddr();
                }
                return "IP_" + ip;
            } catch (Exception ex) {
                return "UNKNOWN";
            }
        }
    }


}
