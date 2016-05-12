package com.btxtech.server.terrain.object;

import com.btxtech.server.collada.ColladaConverterControl;
import com.btxtech.shared.dto.VertexContainer;

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
public class TerrainObjectEntity implements ColladaConverterControl {
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
    private Map<String, VertexContainer.Type> typeMap;

    public String getInternalName() {
        return internalName;
    }

    @Override
    public String getColladaString() {
        return colladaString;
    }

    public void setColladaString(String colladaString) {
        this.colladaString = colladaString;
    }

    @Override
    public int getObjectId() {
        return id.intValue();
    }

    @Override
    public VertexContainer.Type nameToType(String name) {
        for (Map.Entry<String, VertexContainer.Type> entry : typeMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
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
