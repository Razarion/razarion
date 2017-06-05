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

    public TerrainSlopeCorner setPosition(DecimalPosition position) {
        this.position = position;
        return this;
    }

    public Integer getSlopeDrivewayId() {
        return slopeDrivewayId;
    }

    public TerrainSlopeCorner setSlopeDrivewayId(Integer slopeDrivewayId) {
        this.slopeDrivewayId = slopeDrivewayId;
        return this;
    }
}
