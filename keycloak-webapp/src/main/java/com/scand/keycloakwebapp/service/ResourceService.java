package com.scand.keycloakwebapp.service;

import okhttp3.Response;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.representations.account.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ResourceService {

    @Value("${keycloak.auth-server-url}")
    private String basePath;

    @Value("${keycloak.realm}")
    private String realm;

    private final KeycloakRestTemplate keycloakRestTemplate;

    public ResourceService(KeycloakRestTemplate keycloakRestTemplate) {
        this.keycloakRestTemplate = keycloakRestTemplate;
    }

    public List<UserRepresentation> getUsers() throws RuntimeException {
        String url = basePath + "/admin/realms/" + realm + "/users";
        ResponseEntity<UserRepresentation[]> response = keycloakRestTemplate.getForEntity(url, UserRepresentation[].class);

        if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            throw new RuntimeException("Full authentication is required to access this resource");
        } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new RuntimeException("Your credentials have been expired, please login");
        }

        return Arrays.asList(Objects.requireNonNull(response.getBody()));
    }

    public UserRepresentation getUser(String userId) throws RuntimeException {
        String url = basePath + "/admin/realms/" + realm + "/users/" + userId;
        ResponseEntity<UserRepresentation> response = keycloakRestTemplate.getForEntity(url, UserRepresentation.class);

        if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            throw new RuntimeException("Full authentication is required to access this resource");
        } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new RuntimeException("Your credentials have been expired, please login");
        }

        return response.getBody();
    }

    public UserRepresentation createUser(UserRepresentation userRepresentation) throws RuntimeException {
        String url = basePath + "/admin/realms/" + realm + "/users";
        ResponseEntity<Response> response = keycloakRestTemplate.postForEntity(url, userRepresentation, Response.class);

        if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            throw new RuntimeException("Full authentication is required to access this resource");
        } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new RuntimeException("Your credentials have been expired, please login");
        } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
            throw new RuntimeException("User with userName " + userRepresentation.getUsername() + " already exists");
        } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            throw new RuntimeException("Unable to create user with userName " + userRepresentation.getUsername() + ". Malformatted element");
        } else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            throw new RuntimeException("An internal server error occurs");
        }

        List<UserRepresentation> userRepresentations = getUsers();
        for (UserRepresentation user : userRepresentations) {
            if (user.getUsername().equalsIgnoreCase(userRepresentation.getUsername())) {
                return user;
            }
        }
        return userRepresentation;
    }

    public void deleteUser(String userId) {
        keycloakRestTemplate.delete(basePath + "/admin/realms/" + realm + "/users/" + userId);
    }
}
