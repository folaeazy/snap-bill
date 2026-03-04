package com.expenseapp.app.security;

import com.expenseapp.app.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * Filter that validates JWT on every request and sets SecurityContext if valid.
 * Runs once per request (OncePerRequestFilter).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // get token from cookie
        final String jwt = extractTokenFromCookie(request);
        if(jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }


        final String username = jwtUtils.extractIdentifier(jwt);
        try {

            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                //fetch user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                //validate token against loaded user
                if(jwtUtils.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            }

        }catch (JwtException ex) {
                clearAuthCookie(response);
        }
        

        filterChain.doFilter(request, response);
    }

    private void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("AUTH_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if(request.getCookies() == null) return null;
        for(Cookie cookie : request.getCookies()) {
            if("AUTH_TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
