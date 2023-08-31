package com.btxtech.server.persistence.scene;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.BaseItemPlacerConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 18.05.2017.
 */
@Entity
@Table(name = "SCENE_START_POINT_PLACER")
public class StartPointPlacerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "suggestedPositionX")),
            @AttributeOverride(name = "y", column = @Column(name = "suggestedPositionY")),
    })
    private DecimalPosition suggestedPosition;
    private Double enemyFreeRadius;
    @ElementCollection
    @CollectionTable(name = "SCENE_START_PLACE_ALLOWED_AREA", joinColumns = @JoinColumn(name = "startPointPlacer"))
    @OrderColumn(name = "orderColumn")
    private List<DecimalPosition> allowedArea;

    public BaseItemPlacerConfig toStartPointPlacerConfig() {
        if ((allowedArea == null || allowedArea.isEmpty()) && suggestedPosition == null && enemyFreeRadius == null) {
                return null;
        }
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setSuggestedPosition(suggestedPosition).setEnemyFreeRadius(enemyFreeRadius);
        if (allowedArea != null && !allowedArea.isEmpty()) {
            // TODO baseItemPlacerConfig.setAllowedArea(new Polygon2D(allowedArea));
            throw new UnsupportedOperationException("...TODO...");
        }
        return baseItemPlacerConfig;
    }

    public void fromStartPointPlacerConfig(BaseItemPlacerConfig startPointPlacerConfig) {
        suggestedPosition = startPointPlacerConfig.getSuggestedPosition();
        if (startPointPlacerConfig.getAllowedArea() != null) {
            if (allowedArea == null) {
                allowedArea = new ArrayList<>();
            }
            allowedArea.clear();
            // TODO  allowedArea.addAll(startPointPlacerConfig.getAllowedArea().getCorners());
            throw new UnsupportedOperationException("...TODO...");
        } else {
            allowedArea = null;
        }
        enemyFreeRadius = startPointPlacerConfig.getEnemyFreeRadius();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StartPointPlacerEntity that = (StartPointPlacerEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
