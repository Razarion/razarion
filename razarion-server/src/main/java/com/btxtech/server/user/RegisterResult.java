package com.btxtech.server.user;

/**
 * Created by Beat
 * on 02.02.2018.
 */
public enum RegisterResult {
    USER_ALREADY_LOGGED_IN,
    INVALID_EMAIL,
    EMAIL_ALREADY_USED,
    INVALID_PASSWORD,
    OK,
    UNKNOWN_ERROR
}
