package com.btxtech.shared.gameengine.datatypes.itemtype;

import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 18.11.2016.
 */
public class TurretType {
    private double angleVelocity;
    private Vertex torrentCenter;
    private Vertex muzzlePosition;

    public double getAngleVelocity() {
        return angleVelocity;
    }

    public TurretType setAngleVelocity(double angleVelocity) {
        this.angleVelocity = angleVelocity;
        return this;
    }

    public Vertex getTorrentCenter() {
        return torrentCenter;
    }

    public TurretType setTorrentCenter(Vertex torrentCenter) {
        this.torrentCenter = torrentCenter;
        return this;
    }

    public Vertex getMuzzlePosition() {
        return muzzlePosition;
    }

    public TurretType setMuzzlePosition(Vertex muzzlePosition) {
        this.muzzlePosition = muzzlePosition;
        return this;
    }
}
