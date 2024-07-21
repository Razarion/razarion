package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.BrushConfig;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "BRUSH")
public class BrushConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @Lob
    private String brushJson;


    public BrushConfig toConfig() {
        return new BrushConfig()
                .id(id)
                .internalName(internalName)
                .brushJson(brushJson);
    }

    public void fromConfig(BrushConfig config) {
        internalName = config.getInternalName();
        brushJson = config.getBrushJson();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BrushConfigEntity that = (BrushConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
