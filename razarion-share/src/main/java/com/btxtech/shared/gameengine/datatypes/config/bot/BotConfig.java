package com.btxtech.shared.gameengine.datatypes.config.bot;


import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

import java.util.List;

/**
 * User: beat
 * Date: 10.10.2011
 * Time: 13:36:14
 */
public class BotConfig implements ObjectNameIdProvider {
    private int id;
    private String internalName;
    private Integer auxiliaryId;
    private boolean npc;
    private int actionDelay;
    private PlaceConfig realm;
    private String name;
    private boolean autoAttack;
    private Integer minInactiveMs;
    private Integer maxInactiveMs;
    private Integer minActiveMs;
    private Integer maxActiveMs;
    private List<BotEnragementStateConfig> botEnragementStateConfigs;

    public BotConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public Integer getAuxiliaryId() {
        return auxiliaryId;
    }

    public BotConfig setAuxiliaryId(Integer auxiliaryId) {
        this.auxiliaryId = auxiliaryId;
        return this;
    }

    public BotConfig setNpc(boolean npc) {
        this.npc = npc;
        return this;
    }

    public BotConfig setActionDelay(int actionDelay) {
        this.actionDelay = actionDelay;
        return this;
    }

    public BotConfig setRealm(PlaceConfig realm) {
        this.realm = realm;
        return this;
    }

    public BotConfig setName(String name) {
        this.name = name;
        return this;
    }

    public BotConfig setAutoAttack(boolean autoAttack) {
        this.autoAttack = autoAttack;
        return this;
    }

    public BotConfig setMinInactiveMs(Integer minInactiveMs) {
        this.minInactiveMs = minInactiveMs;
        return this;
    }

    public BotConfig setMaxInactiveMs(Integer maxInactiveMs) {
        this.maxInactiveMs = maxInactiveMs;
        return this;
    }

    public BotConfig setMinActiveMs(Integer minActiveMs) {
        this.minActiveMs = minActiveMs;
        return this;
    }

    public BotConfig setMaxActiveMs(Integer maxActiveMs) {
        this.maxActiveMs = maxActiveMs;
        return this;
    }

    public BotConfig setBotEnragementStateConfigs(List<BotEnragementStateConfig> botEnragementStateConfigs) {
        this.botEnragementStateConfigs = botEnragementStateConfigs;
        return this;
    }

    public int getId() {
        return id;
    }

    public BotConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public boolean isNpc() {
        return npc;
    }

    public int getActionDelay() {
        return actionDelay;
    }

    public PlaceConfig getRealm() {
        return realm;
    }

    public String getName() {
        return name;
    }

    public boolean isAutoAttack() {
        return autoAttack;
    }

    public Integer getMinInactiveMs() {
        return minInactiveMs;
    }

    public Integer getMaxInactiveMs() {
        return maxInactiveMs;
    }

    public Integer getMinActiveMs() {
        return minActiveMs;
    }

    public Integer getMaxActiveMs() {
        return maxActiveMs;
    }

    public boolean intervalBot() {
        return minInactiveMs != null || maxInactiveMs != null || minActiveMs != null || maxActiveMs != null;
    }

    public boolean intervalValid() {
        return !(minInactiveMs == null || maxInactiveMs == null || minActiveMs == null || maxActiveMs == null)
                && !(minInactiveMs <= 0 || maxInactiveMs <= 0 || minActiveMs <= 0 || maxActiveMs <= 0)
                && minInactiveMs <= maxInactiveMs
                && minActiveMs <= maxActiveMs;
    }

    public List<BotEnragementStateConfig> getBotEnragementStateConfigs() {
        return botEnragementStateConfigs;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BotConfig botConfig = (BotConfig) o;

        return id == botConfig.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "BotConfig: " + name + " realm: " + realm;
    }
}
