package com.cw.scheduler.ratelimit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int capacity();                 // total tokens in bucket
    int refillTokens();             // tokens added each refill period
    int refillDurationSeconds();    // refill period in seconds
    String message() default "Too many requests, please try again later!";
}