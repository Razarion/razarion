package com.btxtech.server.rest.director;

/**
 * A camera pose (Babylon world space) captured live from the director-mode
 * client and read back by the studio to author a keyframe ("capture current
 * view"). The studio has no engine, so this readback is how it learns where the
 * operator framed a shot.
 */
public class DirectorCameraPose {
    /** Server-stamped on each publish so the studio can detect a FRESH capture
     *  even when the camera didn't move (identical coords, higher seq). */
    private long seq;
    private double posX;
    private double posY;
    private double posZ;
    private double targetX;
    private double targetY;
    private double targetZ;

    public long getSeq() { return seq; }
    public void setSeq(long seq) { this.seq = seq; }
    public double getPosX() { return posX; }
    public void setPosX(double posX) { this.posX = posX; }
    public double getPosY() { return posY; }
    public void setPosY(double posY) { this.posY = posY; }
    public double getPosZ() { return posZ; }
    public void setPosZ(double posZ) { this.posZ = posZ; }
    public double getTargetX() { return targetX; }
    public void setTargetX(double targetX) { this.targetX = targetX; }
    public double getTargetY() { return targetY; }
    public void setTargetY(double targetY) { this.targetY = targetY; }
    public double getTargetZ() { return targetZ; }
    public void setTargetZ(double targetZ) { this.targetZ = targetZ; }
}
