package com.btxtech.server.persistence.ui;

import com.btxtech.server.persistence.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "BRUSH")
public class BrushConfigEntity extends BaseEntity {
    @Lob
    private String brushJson;

    public String getBrushJson() {
        return brushJson;
    }

    public void setBrushJson(String brushJson) {
        this.brushJson = brushJson;
    }
}
