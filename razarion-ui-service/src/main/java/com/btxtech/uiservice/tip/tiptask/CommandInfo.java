package com.btxtech.uiservice.tip.tiptask;

/**
 * Created by Beat
 * 06.01.2017.
 */
public class CommandInfo {
    public enum Type {
        ATTACK,
        FINALIZE_BUILD,
        BUILD,
        FABRICATE,
        HARVEST,
        MOVE,
        PICK_BOX,
        LOAD_CONTAINER,
        UNLOAD_CONTAINER
    }
    private Type type;
    private Integer toBeFinalizedId;
    private Integer toBeBuiltId;
    private Integer synBoxItemId;

    public CommandInfo(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
    Integer getToBeFinalizedId() {
        return toBeFinalizedId;
    }

    Integer getToBeBuiltId() {
        return toBeBuiltId;
    }

    Integer getSynBoxItemId() {
        return synBoxItemId;
    }

    public CommandInfo setToBeFinalizedId(Integer toBeFinalizedId) {
        this.toBeFinalizedId = toBeFinalizedId;
        return this;
    }

    public CommandInfo setToBeBuiltId(Integer toBeBuiltId) {
        this.toBeBuiltId = toBeBuiltId;
        return this;
    }

    public CommandInfo setSynBoxItemId(Integer synBoxItemId) {
        this.synBoxItemId = synBoxItemId;
        return this;
    }
}
