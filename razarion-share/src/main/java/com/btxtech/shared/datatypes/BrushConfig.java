package com.btxtech.shared.datatypes;

import com.btxtech.shared.dto.Config;

public class BrushConfig implements Config {
    private int id;
    private String internalName;
    private String brushJson;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getBrushJson() {
        return brushJson;
    }

    public void setBrushJson(String brushJson) {
        this.brushJson = brushJson;
    }


    public BrushConfig id(int id) {
        this.id = id;
        return this;
    }

    public BrushConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public BrushConfig brushJson(String brushJson) {
        setBrushJson(brushJson);
        return this;
    }

    @Override
    public String toString() {
        return "BrushConfig{" +
                "id=" + id +
                ", internalName='" + internalName + '\'' +
                ", brushJson='" + brushJson + '\'' +
                '}';
    }
}
