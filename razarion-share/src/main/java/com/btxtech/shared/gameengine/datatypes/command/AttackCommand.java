package com.btxtech.shared.gameengine.datatypes.command;


import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import org.dominokit.jackson.annotation.JSONMapper;

/**
 * User: Razarion contributors
 * Date: Aug 1, 2009
 * Time: 1:04:16 PM
 */
@JSONMapper
public class AttackCommand extends PathToDestinationCommand {
    private int target;
    private boolean followTarget;

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public boolean isFollowTarget() {
        return followTarget;
    }

    public void setFollowTarget(boolean followTarget) {
        this.followTarget = followTarget;
    }

    @Override
    public GameConnectionPacket connectionPackage() {
        return GameConnectionPacket.ATTACK_COMMAND;
    }

    @Override
    public String toString() {
        return super.toString() + " target: " + target + " followTarget: " + followTarget;
    }
}