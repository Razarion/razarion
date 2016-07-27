package com.btxtech.shared.gameengine.datatypes.info;

import java.io.Serializable;

/**
 * User: beat
 * Date: 31.05.13
 * Time: 13:44
 */
public class SimpleGuild implements Serializable {
    private int id;
    private String name;

    /**
     * Used by GWT
     */
    protected SimpleGuild() {
    }

    public SimpleGuild(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleGuild that = (SimpleGuild) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "SimpleGuild{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
