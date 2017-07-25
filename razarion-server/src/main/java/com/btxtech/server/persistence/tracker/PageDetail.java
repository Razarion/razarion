package com.btxtech.server.persistence.tracker;

import java.util.Date;

/**
 * Created by Beat
 * on 25.07.2017.
 */
public class PageDetail {
    private Date time;
    private String page;
    private String parameters;
    private String uri;

    public Date getTime() {
        return time;
    }

    public PageDetail setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getPage() {
        return page;
    }

    public PageDetail setPage(String page) {
        this.page = page;
        return this;
    }

    public String getParameters() {
        return parameters;
    }

    public PageDetail setParameters(String parameters) {
        this.parameters = parameters;
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
