package com.btxtech.server.marketing;

import javax.persistence.Embeddable;

/**
 * Created by Beat
 * 20.03.2017.
 */
@Embeddable
public class Interest {
    private String id;
    private String name;

    public Interest() {
    }

    public Interest(Interest interest) {
        id = interest.getId();
        name = interest.getName();
    }

    public String getId() {
        return id;
    }

    public Interest setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Interest setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        return "Interest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
