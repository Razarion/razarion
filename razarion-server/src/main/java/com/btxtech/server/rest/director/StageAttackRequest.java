package com.btxtech.server.rest.director;

/**
 * Stage a filmed battle: spawn a green (OWN/human) strike force at (x, y) and
 * order it to attack the enemy bot. Game-plane coordinates.
 */
public class StageAttackRequest {
    /** Spawn position (game plane) for the strike force. */
    private double x;
    private double y;
    /** How many units to spawn (default 5). */
    private Integer count;
    /** BaseItemType id of the attacker unit; null = auto-pick the first weapon unit. */
    private Integer baseItemTypeId;
    /** Base id to attack; null = auto-pick the first bot base. */
    private Integer targetBaseId;

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public Integer getBaseItemTypeId() { return baseItemTypeId; }
    public void setBaseItemTypeId(Integer baseItemTypeId) { this.baseItemTypeId = baseItemTypeId; }
    public Integer getTargetBaseId() { return targetBaseId; }
    public void setTargetBaseId(Integer targetBaseId) { this.targetBaseId = targetBaseId; }
}
