package com.btxtech.uiservice.cockpit.item;

public abstract class BuildupItemCockpit {
    public String imageUrl;
    public int itemTypeId;
    public String itemTypeName;
    public int price;
    public int itemCount;
    public int itemLimit;
    public boolean enabled;
    public boolean buildLimitReached;
    public boolean buildHouseSpaceReached;
    public boolean buildNoMoney;

    public AngularZoneRunner angularZoneRunner;
    public Object progress;

    @SuppressWarnings("unused") // Called by Angular
    public abstract void onBuild();

    @SuppressWarnings("unused") // Called by Angular
    public abstract void setAngularZoneRunner(AngularZoneRunner angularZoneRunner);

    public abstract void updateState();

    public abstract void updateResources(int resources);

    public void releaseMonitor() {
        // TODO
    }

    public interface AngularZoneCallback {
        @SuppressWarnings("unused")
            // Called by Angular
        void callback();
    }
}
