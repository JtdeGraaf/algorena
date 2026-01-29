package com.algorena.security;

import com.algorena.users.domain.Language;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public SimpleUserPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        Object p = auth.getPrincipal();
        if (p instanceof SimpleUserPrincipal sup) {
            return sup;
        }
        throw new IllegalStateException("Principal is not of type SimpleUserPrincipal");
    }

    public Long id() {
        return Long.parseLong(principal().id());
    }

    public String email() {
        return principal().email();
    }

    public String name() {
        return principal().name();
    }

    public Language language() {
        return principal().language();
    }
}