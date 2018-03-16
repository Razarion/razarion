package com.btxtech.server.persistence.tracker;

import java.util.Date;

/**
 * Created by Beat
 * on 25.07.2017.
 */
public class PageDetail {
    public enum Type {
        SERVER_ACCESS,
        FRONTEND_NAVIGATION,
        WINDOW_CLOSED
    }
    private Date time;
    private Type type;
    private String page;
    private String additional;
    private String uri;

    public Date getTime() {
        return time;
    }

    public PageDetail setTime(Date time) {
        this.time = time;
        return this;
    }

    public Type getType() {
        return type;
    }

    public PageDetail setType(Type type) {
        this.type = type;
        return this;
    }

    public String getPage() {
        return page;
    }

    public PageDetail setPage(String page) {
        this.page = page;
        return this;
    }

    public String getAdditional() {
        return additional;
    }

    public PageDetail setAdditional(String additional) {
        this.additional = additional;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public PageDetail setUri(String uri) {
        this.uri = uri;
        return this;
    }
}
