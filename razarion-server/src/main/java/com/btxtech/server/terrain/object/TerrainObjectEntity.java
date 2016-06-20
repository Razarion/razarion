package com.btxtech.server.terrain.object;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Entity
@Table(name = "TERRAIN_OBJECT")
public class TerrainObjectEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String internalName;
    @Lob
    @Basic(optional = false)
    private String colladaString;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<TerrainObjectMaterialEntity> materials;

    public Long getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getColladaString() {
        return colladaString;
    }

    public void setColladaString(String colladaString) {
        this.colladaString = colladaString;
    }

    public TerrainObjectMaterialEntity getMaterial(String name) {
        for (TerrainObjectMaterialEntity material : materials) {
            if (material.getName().equalsIgnoreCase(name)) {
                return material;
            }
        }
        throw new IllegalArgumentException("No material for name: " + name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainObjectEntity that = (TerrainObjectEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
