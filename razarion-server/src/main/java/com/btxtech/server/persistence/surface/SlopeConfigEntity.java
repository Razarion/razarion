package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.datatypes.DecimalPosition;
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
import java.util.Collections;
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
    private WaterConfigEntity waterConfig;
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private List<SlopeNodeEntity> slopeSkeletonEntries;
    private double outerLineGameEngine;
    private double innerLineGameEngine;
    private double coastDelimiterLineGameEngine;
    private double horizontalSpace;
    private int segments;

    public Integer getId() {
        return id;
    }

    public SlopeConfig toSlopeConfig() {
        SlopeConfig slopeConfig = new SlopeConfig();
        slopeConfig.id(id).internalName(internalName);
        slopeConfig.setSegments(segments);
        slopeConfig.setInnerLineGameEngine(innerLineGameEngine);
        slopeConfig.setCoastDelimiterLineGameEngine(coastDelimiterLineGameEngine);
        slopeConfig.setOuterLineGameEngine(outerLineGameEngine);
        if (this.shape != null) {
            slopeConfig.setSlopeShapes(this.shape.stream().map(SlopeShapeEntity::toSlopeShape).collect(Collectors.toList()));
        } else {
            slopeConfig.setSlopeShapes(Collections.emptyList());
        }
        slopeConfig.setHorizontalSpace(horizontalSpace);
        if (waterConfig != null) {
            slopeConfig.setWaterConfigId(waterConfig.getId());
        }
// TODO       this.shape.stream().map(SlopeShapeEntity::toSlopeShape).collect(Collectors.toList())
// TODO       SlopeNode[][] slopeNodes = new SlopeNode[segments][shape.getVertexCount()];
// TODO       for (SlopeNodeEntity slopeSkeletonEntry : slopeSkeletonEntries) {
// TODO           slopeNodes[slopeSkeletonEntry.getSegmentIndex()][slopeSkeletonEntry.getRowIndex()] = slopeSkeletonEntry.toSlopeNode();
// TODO       }
// TODO       slopeConfig.setSlopeNodes(slopeNodes);
        return slopeConfig;
    }

    public void fromSlopeConfig(SlopeConfig slopeConfig, ImagePersistence imagePersistence) {
//        shape.clear();
//        // TODO  for (SlopeShape slopeShape : slopeConfig.getSlopeShapes()) {
//        // TODO     SlopeShapeEntity slopeShapeEntity = new SlopeShapeEntity();
//        // TODO     slopeShapeEntity.fromSlopeShape(slopeShape);
//        // TODO     shape.add(slopeShapeEntity);
//        // TODO  }
        internalName = slopeConfig.getInternalName();
//        specularLightConfigEmbeddable.fromLightConfig(slopeConfig.getSlopeConfig().getSpecularLightConfig());
//        type = slopeConfig.getSlopeConfig().getType();
//        innerLineGameEngine = slopeConfig.getSlopeConfig().getInnerLineGameEngine();
//        coastDelimiterLineGameEngine = slopeConfig.getSlopeConfig().getCoastDelimiterLineGameEngine();
//        outerLineGameEngine = slopeConfig.getSlopeConfig().getOuterLineGameEngine();
//        texture = imagePersistence.getImageLibraryEntity(slopeConfig.getSlopeConfig().getSlopeTextureId());
//        textureScale = slopeConfig.getSlopeConfig().getSlopeTextureScale();
//        bm = imagePersistence.getImageLibraryEntity(slopeConfig.getSlopeConfig().getSlopeBumpMapId());
//        bmDepth = slopeConfig.getSlopeConfig().getSlopeBumpMapDepth();
//        horizontalSpace = slopeConfig.getSlopeConfig().getHorizontalSpace();
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

    public void setDefault() {
        segments = 1;
        horizontalSpace = 0.5;
        shape = new ArrayList<>();
        shape.add(new SlopeShapeEntity().setPosition(new DecimalPosition(0, 0)).setSlopeFactor(1));
        shape.add(new SlopeShapeEntity().setPosition(new DecimalPosition(1, 1)).setSlopeFactor(1));
        slopeSkeletonEntries = new ArrayList<>();
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
