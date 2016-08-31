package com.btxtech.shared.datatypes;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 30.08.2016.
 */
@Portable
public class UserContext {
    private String name;

    public String getName() {
        return name;
    }

    public UserContext setName(String name) {
        this.name = name;
        return this;
    }
}
