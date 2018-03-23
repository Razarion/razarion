package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Beat
 * on 19.03.2018.
 */
@Dependent
public class BotScene {
    @Inject
    private Instance<BotSceneConflict> conflictInstance;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private BotSceneConfig botSceneConfig;
    private Set<Integer> botsToWatch;
    private HashMap<HumanPlayerId, Mood> moods = new HashMap<>();
    private Collection<BotSceneConflict> botSceneConflicts = new ArrayList<>();
    private SimpleScheduledFuture future;

    public void start(BotSceneConfig botSceneConfig) {
        botsToWatch = new HashSet<>(botSceneConfig.getBotIdsToWatch());
        this.botSceneConfig = botSceneConfig;
        future = simpleExecutorService.scheduleAtFixedRate(botSceneConfig.getScheduleTimeMillis(), true, this::liveOutMood, SimpleExecutorService.Type.BOT_SCENE_TICKER);
    }

    public void onKillBotItem(int botId, SyncBaseItem target, PlayerBase actor) {
        if (botsToWatch.contains(botId)) {
            Mood mood = moods.computeIfAbsent(actor.getHumanPlayerId(), humanPlayerId -> new Mood(actor.getHumanPlayerId()));
            mood.increaseKills();
        }
    }

    public void stop() {
        try {
            future.cancel();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void liveOutMood() {
        moods.values().forEach(mood -> {
            try {
                if (mood.checkThreshold(botSceneConfig)) {
                    BotSceneConflict botSceneConflict = conflictInstance.get();
                    botSceneConflict.init(mood, botSceneConfig.getBotSceneConflictConfig());
                    botSceneConflict.start();
                    botSceneConflicts.add(botSceneConflict);
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        });
        botSceneConflicts.forEach(BotSceneConflict::tick);
        botSceneConflicts.removeIf(botSceneConflict -> {
            if (botSceneConflict.isOver()) {
                botSceneConflict.clean();
                moods.remove(botSceneConflict.getHumanPlayerId());
                return true;
            }
            return false;
        });
    }
}
