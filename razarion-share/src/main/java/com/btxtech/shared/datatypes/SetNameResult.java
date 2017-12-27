package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * on 26.12.2017.
 */
public class SetNameResult {
    private UserContext userContext;
    private ErrorResult errorResult;

    public UserContext getUserContext() {
        return userContext;
    }

    public SetNameResult setUserContext(UserContext userContext) {
        this.userContext = userContext;
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
