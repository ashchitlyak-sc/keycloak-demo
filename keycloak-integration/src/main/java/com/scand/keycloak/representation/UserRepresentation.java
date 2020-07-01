package com.scand.keycloak.representation;

import com.scand.keycloak.domain.User;
import com.scand.keycloak.service.UserService;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

import java.util.List;
import java.util.Map;

public class UserRepresentation extends AbstractUserAdapterFederatedStorage {

    private User user;
    private final UserService userService;

    public UserRepresentation(User user, KeycloakSession session, RealmModel realm, ComponentModel model, UserService userService) {
        super(session, realm, model);
        this.user = user;
        this.userService = userService;
    }

    @Override
    public String getUsername() {
        return this.user.getLogin();
    }

    @Override
    public void setUsername(String username) {
        this.user.setLogin(username);
        this.user = userService.update(user);
    }

    @Override
    public void setEmail(String email) {
        this.user.setEmail(email);
        this.user = userService.update(user);
    }

    @Override
    public String getEmail() {
        return this.user.getEmail();
    }

    @Override
    public void setFirstName(String firstName) {
        this.user.setFirstName(firstName);
        this.user = userService.update(user);
    }

    @Override
    public String getFirstName() {
        return this.user.getFirstName();
    }

    @Override
    public void setLastName(String lastName) {
        this.user.setLastName(lastName);
        this.user = userService.update(user);
    }

    @Override
    public String getLastName() {
        return this.user.getLastName();
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        // use to store any other User attributes (like phone, address, etc) to external database.
        super.setSingleAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        // use to remove any other User attributes (like phone, address, etc) from  external database.
        super.removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        // use to store any other User attributes (like phone, address, etc) to external database.
        super.setAttribute(name, values);
    }

    @Override
    public String getFirstAttribute(String name) {
        // use to get any other User attributes (like phone, address, etc) from external database.
        return super.getFirstAttribute(name);
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        // use to get any other User attributes (like phone, address, etc) from external database.
        return super.getAttributes();
    }

    @Override
    public List<String> getAttribute(String name) {
        // use to get any other User attributes (like phone, address, etc) from external database.
        return super.getAttribute(name);
    }

    @Override
    public String getId() {
        return StorageId.keycloakId(storageProviderModel, user.getId().toString());
    }
}
