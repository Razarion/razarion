package com.btxtech.server.model.history;

import java.util.Date;

/**
 * One row of the game history, stored in the MongoDB {@code game_history} collection. All referenced
 * values are denormalized snapshots taken at write time (base name, item type, crystal counts, ...) so
 * the entry survives deletion of the referenced entities. Fields are nullable and only the ones
 * relevant to a given {@link GameHistoryType} are populated. Bases without a userId are bots.
 */
public class GameHistory {
    private Date serverTime;
    /** TTL anchor: MongoDB deletes this row once expireAt passes (index expireAfterSeconds=0). Null = never. */
    private Date expireAt;
    private GameHistoryType type;
    private GameHistorySource source;
    private String httpSessionId;

    // Actor = the acting / owning party (the user whose base created the item, picked the box, ...).
    private String userId;
    private Integer baseId;
    private String baseName;
    private Integer botId;

    // Target = the affected party for combat events (the defeated base, the destroyed item's owner).
    private String targetUserId;
    private Integer targetBaseId;
    private String targetBaseName;

    // Item / box
    private Integer itemId;
    private Integer itemTypeId;
    private String itemTypeName;
    private Integer boxId;
    private Integer boxItemTypeId;

    // Progression
    private Integer levelNumber;
    private Integer questId;

    // Economy / inventory
    private Integer crystals;
    private Integer deltaCrystals;
    private Integer inventoryId;
    private Integer inventoryArtifactId;
    private Integer inventoryItemCount;
    private Integer inventoryArtifactCount;
    private Integer levelUnlockEntityId;

    // Bot enragement (debug)
    private String botName;
    private String botState;

    // Map position
    private Double x;
    private Double y;

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    public GameHistoryType getType() {
        return type;
    }

    public void setType(GameHistoryType type) {
        this.type = type;
    }

    public GameHistorySource getSource() {
        return source;
    }

    public void setSource(GameHistorySource source) {
        this.source = source;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getBaseId() {
        return baseId;
    }

    public void setBaseId(Integer baseId) {
        this.baseId = baseId;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public Integer getBotId() {
        return botId;
    }

    public void setBotId(Integer botId) {
        this.botId = botId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Integer getTargetBaseId() {
        return targetBaseId;
    }

    public void setTargetBaseId(Integer targetBaseId) {
        this.targetBaseId = targetBaseId;
    }

    public String getTargetBaseName() {
        return targetBaseName;
    }

    public void setTargetBaseName(String targetBaseName) {
        this.targetBaseName = targetBaseName;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Integer itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public Integer getBoxId() {
        return boxId;
    }

    public void setBoxId(Integer boxId) {
        this.boxId = boxId;
    }

    public Integer getBoxItemTypeId() {
        return boxItemTypeId;
    }

    public void setBoxItemTypeId(Integer boxItemTypeId) {
        this.boxItemTypeId = boxItemTypeId;
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(Integer levelNumber) {
        this.levelNumber = levelNumber;
    }

    public Integer getQuestId() {
        return questId;
    }

    public void setQuestId(Integer questId) {
        this.questId = questId;
    }

    public Integer getCrystals() {
        return crystals;
    }

    public void setCrystals(Integer crystals) {
        this.crystals = crystals;
    }

    public Integer getDeltaCrystals() {
        return deltaCrystals;
    }

    public void setDeltaCrystals(Integer deltaCrystals) {
        this.deltaCrystals = deltaCrystals;
    }

    public Integer getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Integer inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Integer getInventoryArtifactId() {
        return inventoryArtifactId;
    }

    public void setInventoryArtifactId(Integer inventoryArtifactId) {
        this.inventoryArtifactId = inventoryArtifactId;
    }

    public Integer getInventoryItemCount() {
        return inventoryItemCount;
    }

    public void setInventoryItemCount(Integer inventoryItemCount) {
        this.inventoryItemCount = inventoryItemCount;
    }

    public Integer getInventoryArtifactCount() {
        return inventoryArtifactCount;
    }

    public void setInventoryArtifactCount(Integer inventoryArtifactCount) {
        this.inventoryArtifactCount = inventoryArtifactCount;
    }

    public Integer getLevelUnlockEntityId() {
        return levelUnlockEntityId;
    }

    public void setLevelUnlockEntityId(Integer levelUnlockEntityId) {
        this.levelUnlockEntityId = levelUnlockEntityId;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getBotState() {
        return botState;
    }

    public void setBotState(String botState) {
        this.botState = botState;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public GameHistory serverTime(Date serverTime) {
        setServerTime(serverTime);
        return this;
    }

    public GameHistory expireAt(Date expireAt) {
        setExpireAt(expireAt);
        return this;
    }

    public GameHistory type(GameHistoryType type) {
        setType(type);
        return this;
    }

    public GameHistory source(GameHistorySource source) {
        setSource(source);
        return this;
    }

    public GameHistory httpSessionId(String httpSessionId) {
        setHttpSessionId(httpSessionId);
        return this;
    }

    public GameHistory userId(String userId) {
        setUserId(userId);
        return this;
    }

    public GameHistory baseId(Integer baseId) {
        setBaseId(baseId);
        return this;
    }

    public GameHistory baseName(String baseName) {
        setBaseName(baseName);
        return this;
    }

    public GameHistory botId(Integer botId) {
        setBotId(botId);
        return this;
    }

    public GameHistory targetUserId(String targetUserId) {
        setTargetUserId(targetUserId);
        return this;
    }

    public GameHistory targetBaseId(Integer targetBaseId) {
        setTargetBaseId(targetBaseId);
        return this;
    }

    public GameHistory targetBaseName(String targetBaseName) {
        setTargetBaseName(targetBaseName);
        return this;
    }

    public GameHistory itemId(Integer itemId) {
        setItemId(itemId);
        return this;
    }

    public GameHistory itemTypeId(Integer itemTypeId) {
        setItemTypeId(itemTypeId);
        return this;
    }

    public GameHistory itemTypeName(String itemTypeName) {
        setItemTypeName(itemTypeName);
        return this;
    }

    public GameHistory boxId(Integer boxId) {
        setBoxId(boxId);
        return this;
    }

    public GameHistory boxItemTypeId(Integer boxItemTypeId) {
        setBoxItemTypeId(boxItemTypeId);
        return this;
    }

    public GameHistory levelNumber(Integer levelNumber) {
        setLevelNumber(levelNumber);
        return this;
    }

    public GameHistory questId(Integer questId) {
        setQuestId(questId);
        return this;
    }

    public GameHistory crystals(Integer crystals) {
        setCrystals(crystals);
        return this;
    }

    public GameHistory deltaCrystals(Integer deltaCrystals) {
        setDeltaCrystals(deltaCrystals);
        return this;
    }

    public GameHistory inventoryId(Integer inventoryId) {
        setInventoryId(inventoryId);
        return this;
    }

    public GameHistory inventoryArtifactId(Integer inventoryArtifactId) {
        setInventoryArtifactId(inventoryArtifactId);
        return this;
    }

    public GameHistory inventoryItemCount(Integer inventoryItemCount) {
        setInventoryItemCount(inventoryItemCount);
        return this;
    }

    public GameHistory inventoryArtifactCount(Integer inventoryArtifactCount) {
        setInventoryArtifactCount(inventoryArtifactCount);
        return this;
    }

    public GameHistory levelUnlockEntityId(Integer levelUnlockEntityId) {
        setLevelUnlockEntityId(levelUnlockEntityId);
        return this;
    }

    public GameHistory botName(String botName) {
        setBotName(botName);
        return this;
    }

    public GameHistory botState(String botState) {
        setBotState(botState);
        return this;
    }

    public GameHistory x(Double x) {
        setX(x);
        return this;
    }

    public GameHistory y(Double y) {
        setY(y);
        return this;
    }
}
