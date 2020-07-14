package com.scand.auth.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialManager;
import org.keycloak.models.UserModel;

import java.util.List;

/**
 * Copied from https://github.com/gwallet/keycloak-sms-authenticator
 */
public class SMSAuthenticatorUtil {

    private static Logger LOG = Logger.getLogger(SMSAuthenticatorUtil.class);

    public static String getAttributeValue(UserModel user, String attributeName) {
        String result = null;
        List<String> values = user.getAttribute(attributeName);
        if(values != null && values.size() > 0) {
            result = values.get(0);
        }

        return result;
    }


    public static String getCredentialValue(UserCredentialManager manager, RealmModel realm, UserModel user, String credentialName) {
        String result = null;
        List<CredentialModel> creds = manager.getStoredCredentials(realm, user);
        for (CredentialModel cred : creds) {
            if(cred.getType().equals(credentialName)) {
                result = cred.getSecretData();
            }
        }

        return result;
    }

    public static String getConfigString(AuthenticatorConfigModel config, String configName) {
        return getConfigString(config, configName, null);
    }

    public static String getConfigString(AuthenticatorConfigModel config, String configName, String defaultValue) {

        String value = defaultValue;

        if (config.getConfig() != null) {
            // Get value
            value = config.getConfig().get(configName);
        }

        return value;
    }

    public static Long getConfigLong(AuthenticatorConfigModel config, String configName) {
        return getConfigLong(config, configName, null);
    }

    public static Long getConfigLong(AuthenticatorConfigModel config, String configName, Long defaultValue) {

        Long value = defaultValue;

        if (config.getConfig() != null) {
            // Get value
            Object obj = config.getConfig().get(configName);
            try {
                value = Long.valueOf((String) obj); // s --> ms
            } catch (NumberFormatException nfe) {
                LOG.error("Can not convert " + obj + " to a number.");
            }
        }

        return value;
    }
}
