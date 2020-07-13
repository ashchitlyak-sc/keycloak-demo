# Keycloak Demo

The goal is to try of [Keycloak](https://www.keycloak.org/) and implement a Demo for that.

## [Keycloak](keycloak)

It is a Keycloak server.

**Database Configuration:**

- Server: **MS SQL**
- Name: **keycloak**
- User: **SA**
- Password: **SA_PASSWORD**

_Note: You can change configuration [here](https://github.com/ashchitlyak-sc/keycloak-demo/blob/master/keycloak/standalone/configuration/standalone.xml#L132)._

**To Run:**

1. Go to `bin` directory.
2. Execute `standalone.bat` or `standalone.sh`.

Keycloak available by http://localhost:8080/auth.

## [Keycloak Integration](keycloak-integration)

It is Java code (Adapters, Providers, Factories, etc) for integrate your own Application (Spring Boot or any Java-based) to Keycloak service.

**How to Use:**

1. Go inside `keycloak-integration` folder.
2. Execute `gradlew jar` to build `*.jar` file.
3. Copy `*.jar` file to `../keycloak/standalone/deployements` to deploy your code.

### User Storage

Try to use an external database for Keycloak Service.

**How to Use:**

1. Login as admin.
2. Click on `User Federation` on left-side bar.
3. Choose `keycloak-user-storage-provider` from the select.
4. Setup MSSQL database settings (name, host, password, etc) and save.
5. Click on `User` on left-side bar.
6. Try to create/update user.

**[KeycloakUserStorageProvider](/keycloak-integration/src/main/java/com/scand/keycloak/provider/KeycloakUserStorageProvider.java)**

**[KeycloakUserStorageProviderFactory](/keycloak-integration/src/main/java/com/scand/keycloak/provider/KeycloakUserStorageProviderFactory.java)**

## [Keycloak Application](keycloak-webapp)

Spring Boot application with usage of **external** keycloak server.

## [Keycloak Embedded Application](keycloak-embedded-webapp)

Spring Boot application with usage of **embedded** keycloak server.

_Based on: [Keycloak Embedded in a Spring Boot Application](https://www.baeldung.com/keycloak-embedded-in-spring-boot-app) tutorial._