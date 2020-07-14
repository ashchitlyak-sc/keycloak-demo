package com.scand.auth.controller;

import com.scand.auth.dto.RegistryDto;
import com.scand.auth.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registry")
    public String registry(@Valid @RequestBody RegistryDto registryDto) {
        return authService.registry(registryDto.getLogin());
    }

    @GetMapping("/refresh_token")
    public String refreshToken(@RequestParam("phone") String phone) {
        // TODO: implement
        return null;
    }
}
