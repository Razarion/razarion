package com.btxtech.server.model.ui;

import com.btxtech.server.service.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

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
