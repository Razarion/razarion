package com.btxtech.uiservice.cockpit.item;

public abstract class OwnMultipleIteCockpit {
    public OwnItemCockpit ownItemCockpit;
    public int count;

    @SuppressWarnings("unused") // Called by Angular
    public abstract void onSelect();
}
