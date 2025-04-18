package com.btxtech.uiservice.cockpit.item;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType
public abstract class BuildupItemCockpit {
    public String imageUrl;
    public int price;
    public int itemCount;
    public int itemLimit;
    public boolean enabled;
    public String tooltip;
    @JsIgnore
    public AngularZoneRunner angularZoneRunner;
    public Object progress;

    @SuppressWarnings("unused") // Called by Angular
    public abstract void onBuild();

    @SuppressWarnings("unused") // Called by Angular
    public abstract void setAngularZoneRunner(AngularZoneRunner angularZoneRunner);

    @JsIgnore
    public abstract void updateState();

    @JsIgnore
    public abstract void updateResources(int resources);

    @JsIgnore
    public void releaseMonitor() {
        // TODO
    }

    @JsFunction
    public interface AngularZoneCallback {
        @SuppressWarnings("unused") // Called by Angular
        void callback();
    }
}
