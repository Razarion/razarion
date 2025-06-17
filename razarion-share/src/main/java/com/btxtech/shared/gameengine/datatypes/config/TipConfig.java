package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

@JsType
public class TipConfig {
    private String tipString;
    private Integer actorItemTypeId;

    public String getTipString() {
        return tipString;
    }

    public void setTipString(String tipString) {
        this.tipString = tipString;
    }

    public @Nullable Integer getActorItemTypeId() {
        return actorItemTypeId;
    }

    public void setActorItemTypeId(@Nullable Integer actorItemTypeId) {
        this.actorItemTypeId = actorItemTypeId;
    }

    public TipConfig tipString(String tipString) {
        setTipString(tipString);
        return this;
    }

    public TipConfig actorItemTypeId(Integer actorItemTypeId) {
        setActorItemTypeId(actorItemTypeId);
        return this;
    }

}
