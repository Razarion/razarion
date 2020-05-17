package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 21.11.2015.
 */
@Entity
@Table(name = "SLOPE_CONFIG")
public class SlopeConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    @OrderColumn(name = "orderColumn")
    private List<SlopeShapeEntity> shape;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private GroundConfigEntity groundConfig;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private WaterConfigEntity waterConfig;
    private double outerLineGameEngine;
    private double innerLineGameEngine;
    private double coastDelimiterLineGameEngine;
    private double horizontalSpace;

    public Integer getId() {
        return id;
    }

    public SlopeConfig toSlopeConfig() {
        SlopeConfig slopeConfig = new SlopeConfig()
                .id(id)
                .internalName(internalName)
                .horizontalSpace(horizontalSpace)
                .innerLineGameEngine(innerLineGameEngine)
                .outerLineGameEngine(outerLineGameEngine)
                .coastDelimiterLineGameEngine(coastDelimiterLineGameEngine);
        if (groundConfig != null) {
            slopeConfig.setGroundConfigId(groundConfig.getId());
        }
        if (waterConfig != null) {
            slopeConfig.setWaterConfigId(waterConfig.getId());
        }
        if (shape != null && !shape.isEmpty()) {
            slopeConfig.setSlopeShapes(shape.stream().map(SlopeShapeEntity::toSlopeShape).collect(Collectors.toList()));
        }
        return slopeConfig;
    }

    public void fromSlopeConfig(SlopeConfig slopeConfig, ImagePersistence imagePersistence, GroundConfigEntity groundConfigEntity) {
//        shape.clear();
//        // TODO  for (SlopeShape slopeShape : slopeConfig.getSlopeShapes()) {
//        // TODO     SlopeShapeEntity slopeShapeEntity = new SlopeShapeEntity();
//        // TODO     slopeShapeEntity.fromSlopeShape(slopeShape);
//        // TODO     shape.add(slopeShapeEntity);
//        // TODO  }
        internalName = slopeConfig.getInternalName();
//        specularLightConfigEmbeddable.fromLightConfig(slopeConfig.getSlopeConfig().getSpecularLightConfig());
//        type = slopeConfig.getSlopeConfig().getType();
        innerLineGameEngine = slopeConfig.getInnerLineGameEngine();
        coastDelimiterLineGameEngine = slopeConfig.getCoastDelimiterLineGameEngine();
        outerLineGameEngine = slopeConfig.getOuterLineGameEngine();
        groundConfig = groundConfigEntity;
        if (shape == null) {
            shape = new ArrayList<>();
        }
        shape.clear();
        if (slopeConfig.getSlopeShapes() != null) {
            shape.addAll(slopeConfig.getSlopeShapes().stream().map(slopeShape -> {
                SlopeShapeEntity slopeShapeEntity = new SlopeShapeEntity();
                slopeShapeEntity.fromSlopeShape(slopeShape);
                return slopeShapeEntity;
            }).collect(Collectors.toList()));
        }
//        texture = imagePersistence.getImageLibraryEntity(slopeConfig.getSlopeConfig().getSlopeTextureId());
//        textureScale = slopeConfig.getSlopeConfig().getSlopeTextureScale();
//        bm = imagePersistence.getImageLibraryEntity(slopeConfig.getSlopeConfig().getSlopeBumpMapId());
//        bmDepth = slopeConfig.getSlopeConfig().getSlopeBumpMapDepth();
        horizontalSpace = slopeConfig.getHorizontalSpace();
//        segments = slopeConfig.getSlopeConfig().getSegments();
//        slopeSkeletonEntries.clear();
//        for (int x = 0; x < segments; x++) {
//            for (int y = 0; y < shape.size(); y++) {
//                SlopeNodeEntity slopeNodeEntity = new SlopeNodeEntity();
//                slopeNodeEntity.fromSlopeNode(x, y, slopeConfig.getSlopeConfig().getSlopeNodes()[x][y]);
//                slopeSkeletonEntries.add(slopeNodeEntity);
//            }
//        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlopeConfigEntity that = (SlopeConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
