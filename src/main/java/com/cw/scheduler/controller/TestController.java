package com.cw.scheduler.controller;

import com.cw.scheduler.ratelimit.RateLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    @RateLimit(capacity = 5, refillTokens = 5, refillDurationSeconds = 60, message = "Too many requests from you. Try again in a minute.")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello, world!");
    }
}
