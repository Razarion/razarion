package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * on 02.06.2017.
 */
public class TerrainSlopeCorner {
    private DecimalPosition position;
    private Integer slopeDrivewayId;

    public DecimalPosition getPosition() {
        return position;
    }

    public void setPosition(DecimalPosition position) {
        this.position = position;
    }

    public Integer getSlopeDrivewayId() {
        return slopeDrivewayId;
    }

    public void setSlopeDrivewayId(Integer slopeDrivewayId) {
        this.slopeDrivewayId = slopeDrivewayId;
    }

    public TerrainSlopeCorner position(DecimalPosition position) {
        setPosition(position);
        return this;
    }

    public TerrainSlopeCorner slopeDrivewayId(Integer slopeDrivewayId) {
        setSlopeDrivewayId(slopeDrivewayId);
        return this;
    }
}
