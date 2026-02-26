package com.expenseapp.app.security;

import com.domain.entities.User;
import com.domain.enums.AuthProvider;
import com.domain.repositories.UserRepository;
import com.expenseapp.app.util.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

            String email = oauthUser.getAttribute("email");
            String name = oauthUser.getAttribute("name");
            String providerUserId = oauthUser.getAttribute("sub"); // Google/Microsoft ID
            String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();

            // Find or create user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setAuthProvider(AuthProvider.valueOf(provider));
                        newUser.setProviderUserId(providerUserId);
                        newUser.setEnabled(true);
                        return userRepository.save(newUser);
                    });

            // Generate JWT
            String jwt = jwtUtils.generateToken(user.getEmail());

            // Redirect to frontend with JWT (or set cookie)
            String redirectUrl = "http://localhost:3000/dashboard?token=" + jwt; // adjust to your frontend
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect("http://localhost:3000/login?error=auth_failed");
        }
    }


}
