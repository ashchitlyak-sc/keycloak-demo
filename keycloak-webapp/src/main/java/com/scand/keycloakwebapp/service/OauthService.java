package com.scand.keycloakwebapp.service;

import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class OauthService {

    @Value("${keycloak.auth-server-url}")
    private String basePath;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    private final KeycloakRestTemplate keycloakRestTemplate;

    public OauthService(KeycloakRestTemplate keycloakRestTemplate) {
        this.keycloakRestTemplate = keycloakRestTemplate;
    }

    public AccessTokenResponse login(String username, String password) {
        String uri = basePath + "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> authParams = new LinkedMultiValueMap<>();
        authParams.add("grant_type", "password");
        authParams.add("username", username);
        authParams.add("password", password);
        authParams.add("client_id", clientId);

        return keycloakRestTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(authParams), AccessTokenResponse.class).getBody();
    }
}
