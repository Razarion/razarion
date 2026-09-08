package com.btxtech.server.rest.director;

import com.btxtech.shared.gameengine.datatypes.Character;

/**
 * A base as the director needs to see it: who it is, and <b>where</b>.
 * <p>
 * The where is the point. A client knows only the units it has been sent, and it is sent what
 * happens near where it is looking - so a camera asked to follow a base on the other side of the
 * planet has nothing to aim at, and sits at the origin looking at nothing. The server has the
 * whole world; handing over the centre lets the studio write a plan that starts by flying there,
 * after which the client has the units and can follow them itself, live and smoothly.
 * <p>
 * Coordinates are game coordinates (x, y on the ground plane), which the studio maps to the
 * renderer's (x, z).
 */
public class DirectorBaseInfo {
    private int baseId;
    private String name;
    private Character character;
    private String userId;
    private int itemCount;
    /** Mean position of the base's items; null when the base has none. */
    private Double centreX;
    private Double centreY;
    /** How far the furthest item sits from that centre - a base's own idea of how wide it is. */
    private Double radius;

    public DirectorBaseInfo() {
    }

    public DirectorBaseInfo(int baseId, String name, Character character, String userId,
                            int itemCount, Double centreX, Double centreY, Double radius) {
        this.baseId = baseId;
        this.name = name;
        this.character = character;
        this.userId = userId;
        this.itemCount = itemCount;
        this.centreX = centreX;
        this.centreY = centreY;
        this.radius = radius;
    }

    public int getBaseId() {
        return baseId;
    }

    public void setBaseId(int baseId) {
        this.baseId = baseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public Double getCentreX() {
        return centreX;
    }

    public void setCentreX(Double centreX) {
        this.centreX = centreX;
    }

    public Double getCentreY() {
        return centreY;
    }

    public void setCentreY(Double centreY) {
        this.centreY = centreY;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }
}
