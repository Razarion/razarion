package com.btxtech.shared.gameengine.datatypes.config.bot;


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.system.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User: beat
 * Date: 10.10.2011
 * Time: 13:36:14
 */
public class BotConfig {
    private Integer id;
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

    public @Nullable Integer getId() {
        return id;
    }

    public void setId(@Nullable Integer id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public Integer getAuxiliaryId() {
        return auxiliaryId;
    }

    public void setAuxiliaryId(Integer auxiliaryId) {
        this.auxiliaryId = auxiliaryId;
    }

    public boolean isNpc() {
        return npc;
    }

    public void setNpc(boolean npc) {
        this.npc = npc;
    }

    public int getActionDelay() {
        return actionDelay;
    }

    public void setActionDelay(int actionDelay) {
        this.actionDelay = actionDelay;
    }

    public @Nullable PlaceConfig getRealm() {
        return realm;
    }

    public void setRealm(@Nullable PlaceConfig realm) {
        this.realm = realm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutoAttack() {
        return autoAttack;
    }

    public void setAutoAttack(boolean autoAttack) {
        this.autoAttack = autoAttack;
    }

    public @Nullable Integer getMinInactiveMs() {
        return minInactiveMs;
    }

    public void setMinInactiveMs(@Nullable Integer minInactiveMs) {
        this.minInactiveMs = minInactiveMs;
    }

    public @Nullable Integer getMaxInactiveMs() {
        return maxInactiveMs;
    }

    public void setMaxInactiveMs(@Nullable Integer maxInactiveMs) {
        this.maxInactiveMs = maxInactiveMs;
    }

    public @Nullable Integer getMinActiveMs() {
        return minActiveMs;
    }

    public void setMinActiveMs(@Nullable Integer minActiveMs) {
        this.minActiveMs = minActiveMs;
    }

    public @Nullable Integer getMaxActiveMs() {
        return maxActiveMs;
    }

    public void setMaxActiveMs(@Nullable Integer maxActiveMs) {
        this.maxActiveMs = maxActiveMs;
    }

    public List<BotEnragementStateConfig> getBotEnragementStateConfigs() {
        return botEnragementStateConfigs;
    }

    public void setBotEnragementStateConfigs(List<BotEnragementStateConfig> botEnragementStateConfigs) {
        this.botEnragementStateConfigs = botEnragementStateConfigs;
    }

    public BotConfig id(Integer id) {
        setId(id);
        return this;
    }

    public BotConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public BotConfig auxiliaryId(Integer auxiliaryId) {
        setAuxiliaryId(auxiliaryId);
        return this;
    }

    public BotConfig npc(boolean npc) {
        setNpc(npc);
        return this;
    }

    public BotConfig actionDelay(int actionDelay) {
        setActionDelay(actionDelay);
        return this;
    }

    public BotConfig realm(PlaceConfig realm) {
        setRealm(realm);
        return this;
    }

    public BotConfig name(String name) {
        setName(name);
        return this;
    }

    public BotConfig autoAttack(boolean autoAttack) {
        setAutoAttack(autoAttack);
        return this;
    }

    public BotConfig minInactiveMs(Integer minInactiveMs) {
        setMinInactiveMs(minInactiveMs);
        return this;
    }

    public BotConfig maxInactiveMs(Integer maxInactiveMs) {
        setMaxInactiveMs(maxInactiveMs);
        return this;
    }

    public BotConfig minActiveMs(Integer minActiveMs) {
        setMinActiveMs(minActiveMs);
        return this;
    }

    public BotConfig maxActiveMs(Integer maxActiveMs) {
        setMaxActiveMs(maxActiveMs);
        return this;
    }

    public BotConfig botEnragementStateConfigs(List<BotEnragementStateConfig> botEnragementStateConfigs) {
        setBotEnragementStateConfigs(botEnragementStateConfigs);
        return this;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotConfig botConfig = (BotConfig) o;

        return id == botConfig.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "BotConfig" + name + "(" + id + "): realm: " + realm;
    }
}
