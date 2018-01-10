package com.btxtech.server.persistence.item;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.Date;

/**
 * Created by Beat
 * on 01.01.2018.
 */
public class ItemTracking {
    public enum Type {
        SERVER_START,
        BASE_CREATED,
        BASE_DELETE,
        BASE_ITEM_SPAWN,
        BASE_ITEM_SPAWN_DIRECTLY,
        BASE_ITEM_BUILT,
        BASE_ITEM_FACTORIZED,
        BASE_ITEM_KILLED,
        BASE_ITEM_REMOVED,
        RESOURCE_ITEM_CREATED,
        RESOURCE_ITEM_DELETED,
        BOX_ITEM_CREATED,
        BOX_ITEM_DELETED
    }

    private Date timeStamp;
    private Type type;
    private Integer targetBaseId;
    private Integer targetBaseBotId;
    private Integer targetHumanPlayerId;
    private Integer actorBaseId;
    private Integer actorBaseBotId;
    private Integer actorHumanPlayerId;
    private Integer itemId;
    private DecimalPosition decimalPosition;
    private Integer itemTypeId;
    private Integer actorItemId;

    /**
     * Lmax Disruptor ringbuffer reuses instances of this clas
     */
    public void clean() {
        timeStamp = null;
        type = null;
        targetBaseId = null;
        targetBaseBotId = null;
        targetHumanPlayerId = null;
        actorBaseId = null;
        actorBaseBotId = null;
        actorHumanPlayerId = null;
        itemId = null;
        decimalPosition = null;
        itemTypeId = null;
        actorItemId = null;
    }


    public Date getTimeStamp() {
        return timeStamp;
    }

    public ItemTracking setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public Type getType() {
        return type;
    }

    public ItemTracking setType(Type type) {
        this.type = type;
        return this;
    }

    public Integer getTargetBaseId() {
        return targetBaseId;
    }

    public ItemTracking setTargetBaseId(Integer targetBaseId) {
        this.targetBaseId = targetBaseId;
        return this;
    }

    public Integer getTargetBaseBotId() {
        return targetBaseBotId;
    }

    public ItemTracking setTargetBaseBotId(Integer targetBaseBotId) {
        this.targetBaseBotId = targetBaseBotId;
        return this;
    }

    public Integer getTargetHumanPlayerId() {
        return targetHumanPlayerId;
    }

    public ItemTracking setTargetHumanPlayerId(Integer targetHumanPlayerId) {
        this.targetHumanPlayerId = targetHumanPlayerId;
        return this;
    }

    public Integer getActorBaseId() {
        return actorBaseId;
    }

    public ItemTracking setActorBaseId(Integer actorBaseId) {
        this.actorBaseId = actorBaseId;
        return this;
    }

    public Integer getActorBaseBotId() {
        return actorBaseBotId;
    }

    public ItemTracking setActorBaseBotId(Integer actorBaseBotId) {
        this.actorBaseBotId = actorBaseBotId;
        return this;
    }

    public Integer getActorHumanPlayerId() {
        return actorHumanPlayerId;
    }

    public ItemTracking setActorHumanPlayerId(Integer actorHumanPlayerId) {
        this.actorHumanPlayerId = actorHumanPlayerId;
        return this;
    }

    public Integer getItemId() {
        return itemId;
    }

    public ItemTracking setItemId(Integer itemId) {
        this.itemId = itemId;
        return this;
    }

    public Integer getItemTypeId() {
        return itemTypeId;
    }

    public DecimalPosition getDecimalPosition() {
        return decimalPosition;
    }

    public ItemTracking setDecimalPosition(DecimalPosition decimalPosition) {
        this.decimalPosition = decimalPosition;
        return this;
    }

    public ItemTracking setItemTypeId(Integer itemTypeId) {
        this.itemTypeId = itemTypeId;
        return this;
    }

    public Integer getActorItemId() {
        return actorItemId;
    }

    public ItemTracking setActorItemId(Integer actorItemId) {
        this.actorItemId = actorItemId;
        return this;
    }
}
