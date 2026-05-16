package com.btxtech.server.model.engine;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Camera framing override for an item's thumbnail render. Written by the studio
 * tool when an admin tunes the per-item framing; read back so re-renders pick
 * up the same pose. Embedded into BASE_ITEM_TYPE — six nullable columns, no
 * separate join.
 *
 * Not part of the BaseItemType DTO on purpose: this is editor metadata, not
 * something the game engine or runtime client needs.
 */
@Embeddable
public class ThumbnailCameraEmbeddable {
    @Column(name = "thumb_alpha")
    private Double alpha;
    @Column(name = "thumb_beta")
    private Double beta;
    @Column(name = "thumb_radius")
    private Double radius;
    @Column(name = "thumb_target_x")
    private Double targetX;
    @Column(name = "thumb_target_y")
    private Double targetY;
    @Column(name = "thumb_target_z")
    private Double targetZ;
    /**
     * Frontend Diplomacy enum name (OWN, ENEMY, FRIEND, RESOURCE, BOX) used at
     * cloneModel3D-time to drive material/team-color. Stored as String because
     * the enum lives only in the TS layer.
     */
    @Column(name = "thumb_diplomacy", length = 16)
    private String diplomacy;

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
