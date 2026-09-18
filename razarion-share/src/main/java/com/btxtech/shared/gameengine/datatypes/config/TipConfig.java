package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

@JsType
public class TipConfig {
    private String tipString;
    private Integer actorItemTypeId;
    /**
     * Whether the tip asks for a group of the actor before the command. Only where the quest needs
     * one: the first attack (365) teaches attacking with a single unit, the group comes with 379.
     */
    private boolean group;

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

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public TipConfig tipString(String tipString) {
        setTipString(tipString);
        return this;
    }

    public TipConfig actorItemTypeId(Integer actorItemTypeId) {
        setActorItemTypeId(actorItemTypeId);
        return this;
    }

    public TipConfig group(boolean group) {
        setGroup(group);
        return this;
    }

}
