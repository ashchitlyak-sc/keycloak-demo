package com.scand.auth.dto;

import javax.validation.constraints.NotNull;

public class RegistryDto {
    @NotNull
    private String login;

    public RegistryDto(@NotNull String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
