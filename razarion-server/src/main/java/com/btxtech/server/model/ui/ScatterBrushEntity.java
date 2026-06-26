package com.btxtech.server.model.ui;

import com.btxtech.server.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * Persisted scatter-brush preset for the terrain object editor. The whole preset (selected terrain
 * object type ids + the numeric brush parameters) is stored as a JSON blob, mirroring
 * {@link BrushConfigEntity} which does the same for the height-map brushes.
 */
@Entity
@Table(name = "SCATTER_BRUSH")
public class ScatterBrushEntity extends BaseEntity {
    @Lob
    private String presetJson;

    public String getPresetJson() {
        return presetJson;
    }

    public void setPresetJson(String presetJson) {
        this.presetJson = presetJson;
    }
}
