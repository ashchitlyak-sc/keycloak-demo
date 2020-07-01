package com.scand.keycloakwebapp.controller;

import com.scand.keycloakwebapp.dto.UserDto;
import com.scand.keycloakwebapp.service.OauthService;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OauthController {

    private final OauthService oauthService;

    public OauthController(OauthService oauthService) {
        this.oauthService = oauthService;
    }

    @PostMapping(path = "/login")
    public AccessTokenResponse login(@RequestBody UserDto user) {
        return oauthService.login(user.getUsername(), user.getPassword());
    }
}
