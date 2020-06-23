package com.scand.keycloak.provider;

import com.scand.keycloak.domain.User;
import com.scand.keycloak.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KeycloakUserStorageProviderFactory implements UserStorageProviderFactory<KeycloakUserStorageProvider> {

    private final Map<String, EntityManagerFactory> entityManagerFactories = new HashMap<>();

    protected static final List<ProviderConfigProperty> configMetadata;

    public static final String DB_CONNECTION_NAME_KEY = "db:connectionName";
    public static final String DB_HOST_KEY = "db:host";
    public static final String DB_DATABASE_KEY = "db:database";
    public static final String DB_USERNAME_KEY = "db:username";
    public static final String DB_PASSWORD_KEY = "db:password";

    static {
        configMetadata = ProviderConfigurationBuilder.create()
            // Name
            .property().name(DB_CONNECTION_NAME_KEY)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Connection Name")
            .defaultValue("test")
            .helpText("Name of the connection, can be chosen individually. Enables connection sharing between providers if the same name is provided. Overrides currently saved connection properties.")
            .add()

            // Host
            .property().name(DB_HOST_KEY)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Database Host")
            .defaultValue("localhost")
            .helpText("Host of the connection")
            .add()

            // Database
            .property().name(DB_DATABASE_KEY)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Database Name")
            .defaultValue("keycloak-webapp")
            .add()

            // Username
            .property().name(DB_USERNAME_KEY)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("Database Username")
            .defaultValue("SA")
            .add()

            // Password
            .property().name(DB_PASSWORD_KEY)
            .type(ProviderConfigProperty.PASSWORD)
            .label("Database Password")
            .defaultValue("SA_PASSWORD")
            .add()
            .build();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public KeycloakUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        Map<String, String> properties = new HashMap<>();
        String dbConnectionName = model.getConfig().getFirst("db:connectionName");

        EntityManagerFactory entityManagerFactory = entityManagerFactories.get(dbConnectionName);
        if (entityManagerFactory == null) {
            MultivaluedHashMap<String, String> config = model.getConfig();
            properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServer2012Dialect");
            properties.put("hibernate.connection.driver_class", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
            properties.put("hibernate.connection.url", String.format("jdbc:sqlserver://%s;databaseName=%s", config.getFirst(DB_HOST_KEY), config.getFirst(DB_DATABASE_KEY)));
            properties.put("hibernate.connection.username", config.getFirst(DB_USERNAME_KEY));
            properties.put("hibernate.connection.password", config.getFirst(DB_PASSWORD_KEY));
            properties.put("hibernate.hbm2ddl.auto", "update");

            PersistenceUnitInfo info = getPersistenceUnitInfo(config.getFirst(DB_DATABASE_KEY));
            EntityManagerFactory newEntityManagerFactory = new HibernatePersistenceProvider().createContainerEntityManagerFactory(info, properties);

            entityManagerFactories.put(dbConnectionName, newEntityManagerFactory);
            return new KeycloakUserStorageProvider(session, model, new UserService(newEntityManagerFactory.createEntityManager()));
        }
        return new KeycloakUserStorageProvider(session, model, new UserService(entityManagerFactory.createEntityManager()));
    }

    @Override
    public void onUpdate(KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
        String oldCnName = oldModel.getConfig().getFirst(DB_CONNECTION_NAME_KEY);
        entityManagerFactories.remove(oldCnName);
        onCreate(session, realm, newModel);
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        MultivaluedHashMap<String, String> configMap = config.getConfig();
        if (StringUtils.isBlank(configMap.getFirst(DB_CONNECTION_NAME_KEY))) {
            throw new ComponentValidationException("Connection name empty");
        }
        if (StringUtils.isBlank(configMap.getFirst(DB_HOST_KEY))) {
            throw new ComponentValidationException("Database host empty");
        }
        if (StringUtils.isBlank(configMap.getFirst(DB_DATABASE_KEY))) {
            throw new ComponentValidationException("Database name empty");
        }
        if (StringUtils.isBlank(configMap.getFirst(DB_USERNAME_KEY))) {
            throw new ComponentValidationException("Database username empty");
        }
        if (StringUtils.isBlank(configMap.getFirst(DB_PASSWORD_KEY))) {
            throw new ComponentValidationException("Database password empty");
        }
    }

    private PersistenceUnitInfo getPersistenceUnitInfo(String name) {
        return new PersistenceUnitInfo() {
            @Override
            public String getPersistenceUnitName() {
                return name;
            }

            @Override
            public String getPersistenceProviderClassName() {
                return "org.hibernate.jpa.HibernatePersistenceProvider";
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return PersistenceUnitTransactionType.RESOURCE_LOCAL;
            }

            @Override
            public DataSource getJtaDataSource() {
                return null;
            }

            @Override
            public DataSource getNonJtaDataSource() {
                return null;
            }

            @Override
            public List<String> getMappingFileNames() {
                return Collections.emptyList();
            }

            @Override
            public List<URL> getJarFileUrls() {
                try {
                    return Collections.list(this.getClass()
                        .getClassLoader()
                        .getResources(""));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public URL getPersistenceUnitRootUrl() {
                return null;
            }

            @Override
            public List<String> getManagedClassNames() {
                List<String> managedClasses = new LinkedList<>();
                managedClasses.add(User.class.getName());
                return managedClasses;
            }

            @Override
            public boolean excludeUnlistedClasses() {
                return false;
            }

            @Override
            public SharedCacheMode getSharedCacheMode() {
                return SharedCacheMode.UNSPECIFIED;
            }

            @Override
            public ValidationMode getValidationMode() {
                return ValidationMode.AUTO;
            }

            @Override
            public Properties getProperties() {
                return new Properties();
            }

            @Override
            public String getPersistenceXMLSchemaVersion() {
                return "2.1";
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }

            @Override
            public void addTransformer(ClassTransformer transformer) {}

            @Override
            public ClassLoader getNewTempClassLoader() {
                return null;
            }
        };
    }

    @Override
    public String getId() {
        return "keycloak-user-storage-provider";
    }
}