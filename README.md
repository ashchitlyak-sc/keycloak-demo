# Keycloak Demo

The goal is to try of [Keycloak](https://www.keycloak.org/) and implement a Demo for that.

## [Keycloak](keycloak)

It is a Keycloak server.

**To Run:**

1. Go to `bin` directory.
2. Execute `standalone.bat` or `standalone.sh`.

## [Keycloak Integration](keycloak-integration)

It is Java classes (Adapters, Providers, etc) for integrate your own Application (Spring Boot or any Java-based) to Keycloak service.

**To Build:**

1. Go inside [keycloak-integration](keycloak-integration).
2. Execute `gradlew jar`.
3. Copy `keycloak-integration-0.0.1-SNAPSHOT.jar` to `../keycloak/standalone/deployements` to deploy your extensions.

### User Storage

Try to use an external database for Keycloak Service.

**[KeycloakUserStorageProvider](/keycloak-integration/src/main/java/com/scand/keycloak/provider/KeycloakUserStorageProvider.java)**

**[KeycloakUserStorageProviderFactory](/keycloak-integration/src/main/java/com/scand/keycloak/provider/KeycloakUserStorageProviderFactory.java)**