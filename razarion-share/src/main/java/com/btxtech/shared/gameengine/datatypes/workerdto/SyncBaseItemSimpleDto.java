package com.btxtech.shared.gameengine.datatypes.workerdto;

/**
 * Created by Beat
 * 05.01.2017.
 * <p>
 * This Object is only used to transport the data from the web worker to the client
 * <p>
 * It is meant to use the 'structured clone algorithm'
 * https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Structured_clone_algorithm
 * <p>
 * GWT strips of unused methods. Getter and setter are not available
 * <p>
 * This is only used on the client side. This is no longer used in the game engine
 */
public class SyncBaseItemSimpleDto extends SyncItemSimpleDto { // Rename to Snapshot or volatile
    private int baseId;
    private double buildup;
    private double health;
    private double constructing;
    private int containingItemCount;
    private double maxContainingRadius;
    private boolean contained;

    public int getBaseId() {
        return baseId;
    }

    public void setBaseId(int baseId) {
        this.baseId = baseId;
    }

    public double getBuildup() {
        return buildup;
    }

    public void setBuildup(double buildup) {
        this.buildup = buildup;
    }

    public boolean checkBuildup() {
        return buildup >= 1.0;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public boolean checkHealth() {
        return health >= 1.0;
    }

    public double getConstructing() {
        return constructing;
    }

    public void setConstructing(double constructing) {
        this.constructing = constructing;
    }

    public boolean checkConstructing() {
        return constructing > 0.0;
    }

    public int getContainingItemCount() {
        return containingItemCount;
    }

    public void setContainingItemCount(int containingItemCount) {
        this.containingItemCount = containingItemCount;
    }

    public double getMaxContainingRadius() {
        return maxContainingRadius;
    }

    public void setMaxContainingRadius(double maxContainingRadius) {
        this.maxContainingRadius = maxContainingRadius;
    }

    public boolean isContained() {
        return contained;
    }

    public void setContained(boolean contained) {
        this.contained = contained;
    }

    public static SyncBaseItemSimpleDto from(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        SyncBaseItemSimpleDto syncBaseItemSimpleDto = new SyncBaseItemSimpleDto();
        syncBaseItemSimpleDto.setId(nativeSyncBaseItemTickInfo.id);
        syncBaseItemSimpleDto.setItemTypeId(nativeSyncBaseItemTickInfo.itemTypeId);
        syncBaseItemSimpleDto.setPosition(NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo));
        //  Matrix4 model is not set. Use this from NativeSyncBaseItemTickInfo
        syncBaseItemSimpleDto.setBaseId(nativeSyncBaseItemTickInfo.baseId);
        syncBaseItemSimpleDto.setBuildup(nativeSyncBaseItemTickInfo.buildup);
        syncBaseItemSimpleDto.setHealth(nativeSyncBaseItemTickInfo.health);
        syncBaseItemSimpleDto.setConstructing(nativeSyncBaseItemTickInfo.constructing);
        syncBaseItemSimpleDto.setContainingItemCount(nativeSyncBaseItemTickInfo.containingItemCount);
        syncBaseItemSimpleDto.setMaxContainingRadius(nativeSyncBaseItemTickInfo.maxContainingRadius);
        syncBaseItemSimpleDto.setContained(nativeSyncBaseItemTickInfo.contained);
        return syncBaseItemSimpleDto;
    }
}
