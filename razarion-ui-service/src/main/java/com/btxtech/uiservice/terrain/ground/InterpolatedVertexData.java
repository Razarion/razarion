package com.btxtech.uiservice.terrain.ground;

import com.btxtech.game.jsre.client.common.DecimalPosition;

/**
 * Created by Beat
 * 26.06.2016.
 */
public class InterpolatedVertexData {
    private final VertexData vertexDataBL;
    private final VertexData vertexDataBR;
    private final VertexData vertexDataTR;
    private final VertexData vertexDataTL;
    private final DecimalPosition normalizedInterpolated;

    public InterpolatedVertexData(VertexData vertexDataBL, VertexData vertexDataBR, VertexData vertexDataTR, VertexData vertexDataTL, DecimalPosition normalizedInterpolated) {
        this.vertexDataBL = vertexDataBL;
        this.vertexDataBR = vertexDataBR;
        this.vertexDataTR = vertexDataTR;
        this.vertexDataTL = vertexDataTL;
        this.normalizedInterpolated = normalizedInterpolated;
    }

    public double getSplatting() {
        double splattingBL = vertexDataBL.getSplatting() * (1.0 - normalizedInterpolated.getX()) * (1.0 - normalizedInterpolated.getY());
        double splattingBR = vertexDataBR.getSplatting() * normalizedInterpolated.getX() * (1.0 - normalizedInterpolated.getY());
        double splattingTR = vertexDataTR.getSplatting() * normalizedInterpolated.getX() * normalizedInterpolated.getY();
        double splattingTL = vertexDataTL.getSplatting() * (1.0 - normalizedInterpolated.getX()) * normalizedInterpolated.getY();

        return splattingBL + splattingBR + splattingTR + splattingTL;
    }
}
