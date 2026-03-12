package com.expenseapp.app.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Map;


public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
            String authorizationRequestBaseUri) {
        this.oAuth2AuthorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, authorizationRequestBaseUri
        );
    }


    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = oAuth2AuthorizationRequestResolver.resolve(request);
        return customize(oAuth2AuthorizationRequest);
    }


    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = oAuth2AuthorizationRequestResolver.resolve(request, clientRegistrationId);
        return customize(oAuth2AuthorizationRequest);
    }


    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req) {

        if (req == null) {
            return null;
        }

        Map<String, Object> params = new HashMap<>(req.getAdditionalParameters());

        params.put("access_type", "offline");
        params.put("prompt", "consent");

        return OAuth2AuthorizationRequest.from(req)
                .additionalParameters(params)
                .build();
    }
}
