package com.btxtech.shared.dto;

import java.util.Objects;

public class UserRequest {
    private String email;
    private String password;
    private boolean rememberMe;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public UserRequest email(String email) {
        setEmail(email);
        return this;
    }

    public UserRequest password(String password) {
        setPassword(password);
        return this;
    }

    public UserRequest rememberMe(boolean rememberMe) {
        setRememberMe(rememberMe);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserRequest that = (UserRequest) o;
        return rememberMe == that.rememberMe && Objects.equals(email, that.email) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, rememberMe);
    }
}
