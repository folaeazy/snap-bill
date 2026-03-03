package com.expenseapp.app.dto;

import com.domain.entities.User;

public record OAuthResult(String email, boolean issueJwt) {

    // first-time login → we need to issue JWT
    public static OAuthResult issueJwt(String email) {
        return new OAuthResult(email, true);
    }

    // account linking → no JWT
    public static OAuthResult linked() {
        return new OAuthResult(null, false);
    }
}
