package com.btxtech.server.rest.ui;

public class MaterialSizeInfo {
    public int id;
    public String name;
    public int size;

    public MaterialSizeInfo() {
    }

    public MaterialSizeInfo(int id, String name, int size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}
