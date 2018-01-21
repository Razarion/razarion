package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * on 26.12.2017.
 */
public class SetNameResult {
    private String userName;
    private ErrorResult errorResult;

    public String getUserName() {
        return userName;
    }

    public SetNameResult setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public ErrorResult getErrorResult() {
        return errorResult;
    }

    public SetNameResult setErrorResult(ErrorResult errorResult) {
        this.errorResult = errorResult;
        return this;
    }
}
