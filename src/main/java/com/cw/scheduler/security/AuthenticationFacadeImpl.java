package com.cw.scheduler.security;

import com.cw.scheduler.entity.User;
import com.cw.scheduler.exception.UserNotFoundException;
import com.cw.scheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authenticationFacade")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFacadeImpl implements AuthenticationFacade{

    private final UserRepository userRepository;

    @Override
    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("No authentication found in security context.");
        }

        return authentication;
    }

    @Override
    public User getCurrentUser() {
        String email = getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found: {}", email);
                    return new UserNotFoundException("User not found with email : " + email);
                });
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
