package com.btxtech.shared.dto;

public class ClientLogRecord {
    private String message;
    private String url;
    private String error;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ClientLogRecord message(String message) {
        setMessage(message);
        return this;
    }

    public ClientLogRecord error(String error) {
        setError(error);
        return this;
    }

    public ClientLogRecord url(String url) {
        setUrl(url);
        return this;
    }
}
