package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

import java.util.List;

/**
 * Created by Beat
 * 09.05.2017.
 */
public class SlavePlanetConfig {
    private PlaceConfig startRegion;
    private DecimalPosition noBaseViewPosition;
    private boolean findFreePosition;
    private List<DecimalPosition> positionPath;
    private Double positionRadius;
    private Integer positionMaxItems;

    public PlaceConfig getStartRegion() {
        return startRegion;
    }

    public void setStartRegion(PlaceConfig startRegion) {
        this.startRegion = startRegion;
    }

    public SlavePlanetConfig startRegion(PlaceConfig startRegion) {
        setStartRegion(startRegion);
        return this;
    }

    public DecimalPosition getNoBaseViewPosition() {
        return noBaseViewPosition;
    }

    public void setNoBaseViewPosition(DecimalPosition noBaseViewPosition) {
        this.noBaseViewPosition = noBaseViewPosition;
    }

    public boolean isFindFreePosition() {
        return findFreePosition;
    }

    public void setFindFreePosition(boolean findFreePosition) {
        this.findFreePosition = findFreePosition;
    }

    public SlavePlanetConfig findFreePosition(boolean findFreePosition) {
        setFindFreePosition(findFreePosition);
        return this;
    }

    public List<DecimalPosition> getPositionPath() {
        return positionPath;
    }

    public void setPositionPath(List<DecimalPosition> positionPath) {
        this.positionPath = positionPath;
    }

    public SlavePlanetConfig positionPath(List<DecimalPosition> positionPath) {
        setPositionPath(positionPath);
        return this;
    }

    public Double getPositionRadius() {
        return positionRadius;
    }

    public void setPositionRadius(Double positionRadius) {
        this.positionRadius = positionRadius;
    }

    public SlavePlanetConfig positionRadius(Double positionRadius) {
        setPositionRadius(positionRadius);
        return this;
    }

    public Integer getPositionMaxItems() {
        return positionMaxItems;
    }

    public void setPositionMaxItems(Integer positionMaxItems) {
        this.positionMaxItems = positionMaxItems;
    }

    public SlavePlanetConfig positionMaxItems(Integer positionMaxItem) {
        setPositionMaxItems(positionMaxItem);
        return this;
    }
}
