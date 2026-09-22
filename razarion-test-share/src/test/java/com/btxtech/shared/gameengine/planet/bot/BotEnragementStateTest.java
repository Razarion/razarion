package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * The signal {@link BotRunner#enrageOnKill} needs: did this kill move the bot up a state.
 *
 * On it hangs whether the new state's units go up at once or only on the next pass of the bot
 * ticker, which is actionDelay away - three seconds for the six passive "RazCore Extractor" bots.
 * Measured on PROD 22.09.2026, 19:09-19:27: one level 8 player held all six extractor spawns with
 * about seven vipers and made 19 kills in 18 minutes, killing each extractor again before the
 * tesla that was supposed to answer him had been put on the ground. Twelve players on level 1 and
 * 2 were online at the time, for whom those extractors are the first target the game gives them.
 */
public class BotEnragementStateTest {
    private static final int ENRAGE_UP_KILLS = 5;

    private BotEnragementState enragementState;
    private PlayerBase camper;
    private SyncBaseItem killedBotItem;

    @Before
    public void setUp() {
        camper = new PlayerBase(1, "Camper", null, 0, 0, "954a7de8", null, 0);
        PlayerBase botBase = new PlayerBase(2, "Bot1 Extractor 4", null, 0, 0, null, 1, 0);
        // Stand-ins rather than mocks: both classes only store their collaborators in the
        // constructor and use none of them here, and easymock cannot mock classes without
        // objenesis on the test classpath.
        killedBotItem = new SyncBaseItem(null, null, null, null, null, null, null, null, null, null,
                null, null, null) {
            @Override
            public PlayerBase getBase() {
                return botBase;
            }
        };
    }

    private void startBot(BotEnragementStateConfig... states) {
        BotItemContainer container = new BotItemContainer(null, null, null, null, null) {
            @Override
            public void init(Collection<BotItemConfig> botItems, PlaceConfig realm, String botName,
                             String botInternalName, boolean groundBoxEnabled) {
                // Nothing to build: this is about the ladder, not about the units on it.
            }

            @Override
            void killAllItems(PlayerBase playerBase) {
            }
        };
        enragementState = new BotEnragementState(() -> container);
        enragementState.init(Arrays.asList(states), new PlaceConfig(), "Bot1 Extractor 4",
                "bot1-extractor-4", false, null);
    }

    private BotEnragementStateConfig state(String name, Integer enrageUpKills) {
        return new BotEnragementStateConfig().name(name).enrageUpKills(enrageUpKills)
                .botItems(Collections.<BotItemConfig>emptyList());
    }

    @Test
    public void theKillThatEnragesSaysSo() {
        startBot(state("Extractor", ENRAGE_UP_KILLS), state("Tesla", null));

        for (int i = 0; i < ENRAGE_UP_KILLS - 1; i++) {
            Assert.assertFalse("kill " + (i + 1) + " of " + ENRAGE_UP_KILLS + " does not enrage yet",
                    enragementState.enrageOnKill(killedBotItem, camper));
        }
        Assert.assertTrue("the last kill enrages, and the tesla has to go up in this moment",
                enragementState.enrageOnKill(killedBotItem, camper));
    }

    /** A bot at the top of its ladder, and one that has no ladder, never report a step up. */
    @Test
    public void aBotWithNothingAboveItNeverEnrages() {
        startBot(state("Normal", null));

        for (int i = 0; i < 10; i++) {
            Assert.assertFalse(enragementState.enrageOnKill(killedBotItem, camper));
        }
    }
}
