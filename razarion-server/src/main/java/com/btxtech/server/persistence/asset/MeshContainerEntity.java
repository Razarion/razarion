package com.btxtech.server.persistence.asset;

import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.shared.datatypes.asset.MeshContainer;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

import static com.btxtech.server.persistence.PersistenceUtil.toConfigList;

@Entity
@Table(name = "ASSET_MESH_CONTAINER")
public class MeshContainerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    private String guid;
    @OneToOne
    @JoinColumn(name = "parent_id")
    private MeshContainerEntity parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeshContainerEntity> children;
    @Embedded
    private MeshEmbeddable mesh;

    public Integer getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    public MeshContainer toConfig() {
        return new MeshContainer()
                .id(id)
                .internalName(internalName)
                .guid(guid)
                .mesh(PersistenceUtil.toConfig(mesh, MeshEmbeddable::toMesh))
                .children(toConfigList(children, MeshContainerEntity::toConfig));
    }

    public void fromConfig(MeshContainer meshContainer, MeshContainerEntity parent, ThreeJsModelCrudPersistence threeJsModelCrudPersistence) {
        internalName = meshContainer.getInternalName();
        guid = meshContainer.getGuid();
        children = PersistenceUtil.fromConfigs(children,
                meshContainer.getChildren(),
                MeshContainerEntity::new,
                (childEntity, childConfig) -> childEntity.fromConfig(childConfig, this, threeJsModelCrudPersistence));
        this.parent = parent;
        mesh = PersistenceUtil.fromConfig(mesh,
                meshContainer.getMesh(),
                MeshEmbeddable::new,
                (meshEmbeddable, mesh) -> meshEmbeddable.fromMesh(mesh, threeJsModelCrudPersistence));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MeshContainerEntity that = (MeshContainerEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
