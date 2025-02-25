package com.btxtech.server.persistence.ui;

import com.btxtech.server.persistence.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "TERRAIN_OBJECT_GENERATOR")
public class TerrainObjectGeneratorEntity extends BaseEntity {
    @Lob
    private String generatorJson;

    public String getGeneratorJson() {
        return generatorJson;
    }

    public void setGeneratorJson(String brushJson) {
        this.generatorJson = brushJson;
    }
}
