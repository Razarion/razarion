package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: beat
 * Date: 14.06.12
 * Time: 12:44
 */
@Dependent
public class BotEnragementState {
    public interface Listener {
        void onEnrageNormal(String botName, BotEnragementStateConfig botEnragementStateConfig);

        void onEnrageUp(String botName, BotEnragementStateConfig botEnragementStateConfig, PlayerBase actor);
    }

    @Inject
    private Instance<BotItemContainer> containerInstance;
    private List<BotEnragementStateConfig> botEnragementStateConfigs;
    private BotEnragementStateConfig currentBotEnragementStateConfig;
    private boolean isEnragementActive;
    private BotItemContainer botItemContainer;
    private PlaceConfig realm;
    private String botName;
    private Map<PlayerBase, Integer> killsPerBase = new HashMap<>();
    private Listener listener;

    public void init(List<BotEnragementStateConfig> botEnragementStateConfigs, PlaceConfig realm, String botName, Listener listener) {
        this.botEnragementStateConfigs = botEnragementStateConfigs;
        this.realm = realm;
        this.botName = botName;
        this.listener = listener;
        if (botEnragementStateConfigs.isEmpty()) {
            throw new IllegalArgumentException("Bot must have at least one enragement state configured: " + botName);
        }
        activateEnragementState(botEnragementStateConfigs.get(0), null);
    }

    public void work(PlayerBaseFull base) {
        botItemContainer.work(base);
    }

    public void killAllItems(PlayerBase base) {
        botItemContainer.killAllItems(base);
    }

    public Collection<BotSyncBaseItem> getAllIdleItems() {
        return botItemContainer.getAllIdleItems();
    }

    public void onSyncBaseItemCreated(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        botItemContainer.onSyncBaseItemCreated(syncBaseItem, createdBy);
    }

    private void activateEnragementState(BotEnragementStateConfig botEnragementStateConfig, PlayerBase base) {
        if (base != null && currentBotEnragementStateConfig != null) {
            botItemContainer.killAllItems(base);
        }
        currentBotEnragementStateConfig = botEnragementStateConfig;
        botItemContainer = containerInstance.get();
        botItemContainer.init(botEnragementStateConfig.getBotItems(), realm, botName);
        killsPerBase.clear();
        isEnragementActive = currentBotEnragementStateConfig.getEnrageUpKills() != null && botEnragementStateConfigs.indexOf(currentBotEnragementStateConfig) + 1 < botEnragementStateConfigs.size();
    }

    public void handleIntruders(Collection<SyncBaseItem> allIntruders, PlayerBase botBase) {
        if (allIntruders.isEmpty()) {
            if (!currentBotEnragementStateConfig.equals(botEnragementStateConfigs.get(0))) {
                BotEnragementStateConfig normalState = botEnragementStateConfigs.get(0);
                activateEnragementState(normalState, botBase);
                if (listener != null) {
                    listener.onEnrageNormal(botName, normalState);
                }
            }
        }
        Set<PlayerBase> intruderBases = getAllBases(allIntruders);
        killsPerBase.keySet().removeIf(oldIntruder -> !intruderBases.contains(oldIntruder));
    }

    private Set<PlayerBase> getAllBases(Collection<SyncBaseItem> allIntruders) {
        Set<PlayerBase> bases = new HashSet<>();
        for (SyncBaseItem intruder : allIntruders) {
            bases.add(intruder.getBase());
        }
        return bases;
    }

    void enrageOnKill(SyncBaseItem target, PlayerBase actor) {
        if (isEnragementActive) {
            Integer kills = killsPerBase.get(actor);
            if (kills == null) {
                kills = 0;
            }
            kills = kills + 1;
            killsPerBase.put(actor, kills);
            if (kills >= currentBotEnragementStateConfig.getEnrageUpKills()) {
                BotEnragementStateConfig nextState = botEnragementStateConfigs.get(botEnragementStateConfigs.indexOf(currentBotEnragementStateConfig) + 1);
                activateEnragementState(nextState, target.getBase());
                if (listener != null) {
                    listener.onEnrageUp(botName, nextState, actor);
                }
            }
            // TODO remove the killed bot item from the botItemContainer here instead of iterating over and removing the death items
        }
    }

    public void executeCommand(AbstractBotCommandConfig botCommandConfig, PlayerBaseFull base) {
        botItemContainer.executeCommand(botCommandConfig, base);
    }

    public void attack(SyncBaseItem target) {
        Collection<BotSyncBaseItem> idleAttacker = botItemContainer.getAllIdleItems(target, BotSyncBaseItem::isAbleToAttack);
        idleAttacker.forEach(attacker -> attacker.attack(target));
    }
}
