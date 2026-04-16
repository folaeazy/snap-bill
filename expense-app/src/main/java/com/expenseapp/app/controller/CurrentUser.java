package com.expenseapp.app.controller;

import com.domain.entities.User;
import com.expenseapp.app.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    protected  User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser au)) {
            throw new IllegalStateException("User not authenticated");
        }
        return au.getUser();
    }
}
