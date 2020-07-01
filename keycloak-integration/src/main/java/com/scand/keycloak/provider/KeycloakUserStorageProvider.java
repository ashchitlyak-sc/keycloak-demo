package com.scand.keycloak.provider;

import com.scand.keycloak.domain.User;
import com.scand.keycloak.representation.UserRepresentation;
import com.scand.keycloak.service.UserService;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakUserStorageProvider implements UserStorageProvider, UserLookupProvider, UserQueryProvider, UserRegistrationProvider {

    private final UserService userService;
    private final KeycloakSession session;
    private final ComponentModel model;

    public KeycloakUserStorageProvider(KeycloakSession session, ComponentModel model, UserService userService) {
        this.session = session;
        this.model = model;
        this.userService = userService;
    }

    @Override
    public UserModel getUserById(String keycloakId, RealmModel realm) {
        User user = userService.getUserById(StorageId.externalId(keycloakId));
        if (user == null) {
            return null;
        }

        return getUserRepresentation(user, realm);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        User user = userService.getUserByLogin(username);
        if (user == null) {
            return null;
        }

        return getUserRepresentation(user, realm);
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return null;
        }

        return getUserRepresentation(user, realm);
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        return userService.getCount();
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm) {
        return userService.getUsers()
            .stream()
            .map(user -> getUserRepresentation(user, realm))
            .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> getUsers(RealmModel realm, int first, int max) {
        return userService.getUsers(first, max)
            .stream()
            .map(user -> getUserRepresentation(user, realm))
            .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(String term, RealmModel realm) {
        return userService.searchForUserByUsernameOrEmail(term)
            .stream()
            .map(user -> getUserRepresentation(user, realm))
            .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(String term, RealmModel realm, int first, int max) {
        return userService.searchForUserByUsernameOrEmail(term, first, max)
            .stream()
            .map(user -> getUserRepresentation(user, realm))
            .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
        return userService.getUsers()
            .stream()
            .map(user -> getUserRepresentation(user, realm))
            .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult, int maxResults) {
        return userService.getUsers()
            .stream()
            .map(user -> getUserRepresentation(user, realm))
            .collect(Collectors.toList());
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
        // todo: will be implemented asap
        return Collections.emptyList();
    }

    @Override
    public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {
        // todo: will be implemented asap
        return Collections.emptyList();
    }

    @Override
    public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {
        // todo: will be implemented asap
        return Collections.emptyList();
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        User user = new User();
        user.setLogin(username);

        User createdUser = userService.create(user);
        return getUserRepresentation(createdUser, realm);
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel userModel) {
        User user = userService.getUserById(StorageId.externalId(userModel.getId()));
        if (user == null) {
            return false;
        }

        userService.delete(user);
        return true;
    }

    @Override
    public void close() {
        userService.close();
    }

    private UserRepresentation getUserRepresentation(User user, RealmModel realm) {
        return new UserRepresentation(user, session, realm, model, userService);
    }
}
