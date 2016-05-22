package com.btxtech.server.terrain.object;

import com.btxtech.shared.dto.TerrainObject;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Map;

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
    @ElementCollection
    @CollectionTable(name = "TERRAIN_OBJECT_TYPE_MAP", joinColumns = @JoinColumn(name = "OWNER_ID"))
    @Enumerated(EnumType.STRING)
    private Map<String, TerrainObject.Type> typeMap;

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

    public TerrainObject.Type nameToType(String name) {
        for (Map.Entry<String, TerrainObject.Type> entry : typeMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("No type for name: " + name);
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
