package com.btxtech.uiservice.cockpit.item;

public abstract class ItemContainerCockpit {
    public AngularZoneRunner angularZoneRunner;
    public int count;

    @SuppressWarnings("unused") // Called by Angular
    public abstract void onUnload();

    @SuppressWarnings("unused") // Called by Angular
    public abstract void setAngularZoneRunner(AngularZoneRunner angularZoneRunner);

}
