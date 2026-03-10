package com.expenseapp.app.security;

import com.domain.entities.EmailAccount;
import com.domain.entities.User;
import com.domain.enums.AuthProvider;
import com.domain.enums.ConnectionStatus;
import com.domain.enums.EmailProvider;
import com.domain.repositories.EmailAccountRepository;
import com.domain.repositories.UserRepository;
import com.expenseapp.app.dto.OAuthResult;
import com.expenseapp.app.service.OAuthService;
import com.expenseapp.app.util.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final EmailAccountRepository emailAccountRepository;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final OAuthService oAuthService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {

            return ;
        }

        OAuthResult result = oAuthService.handle(authentication);
        if(result.issueJwt()){
            // Generate JWT
            String jwt = jwtUtils.generateToken(result.email());
            // Create cookie
            Cookie cookie = new Cookie("AUTH_TOKEN", jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setMaxAge(60 * 30 ); // 30min
            cookie.setPath("/");
            response.addCookie(cookie);
            response.sendRedirect("http://localhost:3000/dashboard"); // client domain

        }else {
            response.sendRedirect("http://localhost:3000/dashboard?linked=true");
        }

    }

}
