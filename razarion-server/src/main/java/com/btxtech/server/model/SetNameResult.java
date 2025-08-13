package com.btxtech.server.model;

public class SetNameResult {
    private String userName;
    private SetNameError setNameError;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public SetNameError getErrorResult() {
        return setNameError;
    }

    public void setErrorResult(SetNameError setNameError) {
        this.setNameError = setNameError;
    }

    public SetNameResult errorResult(SetNameError setNameError) {
        setErrorResult(setNameError);
        return this;
    }

    public SetNameResult userName(String userName) {
        setUserName(userName);
        return this;
    }
}
