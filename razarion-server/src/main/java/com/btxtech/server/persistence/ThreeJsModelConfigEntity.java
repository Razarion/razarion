package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 02.05.2016.
 */
@Entity
@Table(name = "THREE_JS_MODEL")
public class ThreeJsModelConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;

    public ThreeJsModelConfig toConfig() {
        return new ThreeJsModelConfig().id(id).internalName(internalName);
    }

    public void from(ThreeJsModelConfig config) {
        this.internalName = config.getInternalName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ThreeJsModelConfigEntity that = (ThreeJsModelConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
