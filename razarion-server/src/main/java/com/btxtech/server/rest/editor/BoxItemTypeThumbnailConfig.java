package com.btxtech.server.rest.editor;

/**
 * Studio-only camera framing override for a BoxItemType's thumbnail render.
 * Lives in razarion-server (not razarion-share) — the game engine never sees
 * this metadata, only the studio editor does.
 *
 * Mirrors BaseItemTypeThumbnailConfig and ResourceItemTypeThumbnailConfig.
 */
public class BoxItemTypeThumbnailConfig {
    private int boxItemTypeId;
    private String internalName;
    private Integer model3DId;
    private Integer thumbnailImageId;
    private Double alpha;
    private Double beta;
    private Double radius;
    private Double targetX;
    private Double targetY;
    private Double targetZ;
    /** Diplomacy enum name (OWN/ENEMY/FRIEND/RESOURCE/BOX) for material tint. */
    private String diplomacy;

    public int getBoxItemTypeId() { return boxItemTypeId; }
    public void setBoxItemTypeId(int boxItemTypeId) { this.boxItemTypeId = boxItemTypeId; }
    public String getInternalName() { return internalName; }
    public void setInternalName(String internalName) { this.internalName = internalName; }
    public Integer getModel3DId() { return model3DId; }
    public void setModel3DId(Integer model3DId) { this.model3DId = model3DId; }
    public Integer getThumbnailImageId() { return thumbnailImageId; }
    public void setThumbnailImageId(Integer thumbnailImageId) { this.thumbnailImageId = thumbnailImageId; }
    public Double getAlpha() { return alpha; }
    public void setAlpha(Double alpha) { this.alpha = alpha; }
    public Double getBeta() { return beta; }
    public void setBeta(Double beta) { this.beta = beta; }
    public Double getRadius() { return radius; }
    public void setRadius(Double radius) { this.radius = radius; }
    public Double getTargetX() { return targetX; }
    public void setTargetX(Double targetX) { this.targetX = targetX; }
    public Double getTargetY() { return targetY; }
    public void setTargetY(Double targetY) { this.targetY = targetY; }
    public Double getTargetZ() { return targetZ; }
    public void setTargetZ(Double targetZ) { this.targetZ = targetZ; }
    public String getDiplomacy() { return diplomacy; }
    public void setDiplomacy(String diplomacy) { this.diplomacy = diplomacy; }
}
