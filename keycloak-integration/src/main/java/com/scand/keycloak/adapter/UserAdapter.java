package com.scand.keycloak.adapter;

import com.scand.keycloak.domain.User;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

public class UserAdapter extends AbstractUserAdapterFederatedStorage {

    protected User user;

    public UserAdapter(User user, KeycloakSession session, RealmModel realm, ComponentModel model) {
        super(session, realm, model);
        this.user = user;
    }

    public String getPassword() {
        return this.user.getPassword();
    }

    public void setPassword(String password) {
        this.user.setPassword(password);
    }

    @Override
    public String getUsername() {
        return this.user.getLogin();
    }

    @Override
    public void setUsername(String username) {
        this.user.setLogin(username);
    }

    @Override
    public String getLastName() {
        return this.user.getLastName();
    }

    @Override
    public void setLastName(String lastName) {
        this.user.setLastName(lastName);
    }

    @Override
    public String getFirstName() {
        return this.user.getFirstName();
    }

    @Override
    public void setFirstName(String firstName) {
        this.user.setFirstName(firstName);
    }

    @Override
    public void setEmail(String email) {
        this.user.setEmail(email);
    }

    @Override
    public String getEmail() {
        return this.user.getEmail();
    }

    @Override
    public String getId() {
        return StorageId.keycloakId(storageProviderModel, user.getId().toString());
    }
}
