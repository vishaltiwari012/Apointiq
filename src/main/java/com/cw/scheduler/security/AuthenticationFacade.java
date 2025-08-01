package com.cw.scheduler.security;

import com.cw.scheduler.entity.User;
import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {
    Authentication getAuthentication();
    User getCurrentUser();
}