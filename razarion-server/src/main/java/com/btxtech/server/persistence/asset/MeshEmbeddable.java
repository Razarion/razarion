package com.btxtech.server.persistence.asset;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.shared.datatypes.asset.Mesh;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

@Embeddable
public class MeshEmbeddable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity shape3D;
    private String element3DId;
    @Embedded
    private ShapeTransformEmbeddable shapeTransform;

    public Mesh toMesh() {
        return new Mesh()
                .shape3DId(extractId(shape3D, ColladaEntity::getId))
                .element3DId(element3DId)
                .shapeTransform(PersistenceUtil.toConfig(shapeTransform, ShapeTransformEmbeddable::toShapeTransform));
    }

    public void fromMesh(Mesh mesh, Shape3DCrudPersistence shape3DCrudPersistence) {
        shape3D = shape3DCrudPersistence.getEntity(mesh.getShape3DId());
        element3DId = mesh.getElement3DId();
        shapeTransform = PersistenceUtil.fromConfig(shapeTransform,
                mesh.getShapeTransform(),
                ShapeTransformEmbeddable::new,
                ShapeTransformEmbeddable::fromShapeTransform);
    }
}
