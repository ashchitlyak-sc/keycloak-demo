package com.scand.auth.service;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resources.KeycloakApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private static final String REALM_ID = "scand";

    public String registry(String login) {
        KeycloakSession session = KeycloakApplication.createSessionFactory().create();

        try {
            session.getTransactionManager().begin();

            RealmModel realm = session.realms().getRealm(REALM_ID);
            session.getContext().setRealm(realm);

            UserModel user = session.users().addUser(realm, login);
            user.setEnabled(true);

            session.getTransactionManager().commit();

            return user.getId();
        } catch (Exception ex) {
            LOG.warn("Couldn't register a new user ", ex);
            session.getTransactionManager().rollback();
        } finally {
            session.close();
        }
        throw new RuntimeException("User registration is not success");
    }
}
