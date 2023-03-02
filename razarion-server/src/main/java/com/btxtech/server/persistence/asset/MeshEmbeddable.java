package com.btxtech.server.persistence.asset;

import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.server.persistence.ThreeJsModelConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.shared.datatypes.asset.Mesh;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

import java.util.List;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

@Embeddable
public class MeshEmbeddable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelConfigEntity threeJsModelConfig;
    private String element3DId;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "mesh_conatiner_id", nullable = false)
    @OrderColumn
    private List<ShapeTransformEntity> shapeTransforms;

    public Mesh toMesh() {
        return new Mesh()
                .threeJsModelId(extractId(threeJsModelConfig, ThreeJsModelConfigEntity::getId))
                .element3DId(element3DId)
                .shapeTransforms(PersistenceUtil.toConfigList(shapeTransforms, ShapeTransformEntity::toShapeTransform));
    }

    public void fromMesh(Mesh mesh, ThreeJsModelCrudPersistence threeJsModelCrudPersistence) {
        threeJsModelConfig = threeJsModelCrudPersistence.getEntity(mesh.getThreeJsModelId());
        element3DId = mesh.getElement3DId();
        shapeTransforms = PersistenceUtil.fromConfigs(shapeTransforms,
                mesh.getShapeTransforms(),
                ShapeTransformEntity::new,
                ShapeTransformEntity::fromShapeTransform);
    }
}
