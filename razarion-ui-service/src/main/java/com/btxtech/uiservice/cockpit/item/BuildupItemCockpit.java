package com.btxtech.uiservice.cockpit.item;

import jsinterop.annotations.JsType;

@JsType
public abstract class BuildupItemCockpit {
    public String imageUrl;
    public int price;
    public int itemCount;
    public int itemLimit;
    public boolean enabled;
    public String tooltip;
    public Object progress;

    @SuppressWarnings("unused") // Called by Angular
    public abstract void onBuild();
}
