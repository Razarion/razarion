package com.btxtech.e2e.smoke;

import com.btxtech.e2e.base.AdminApiClient;
import com.btxtech.e2e.base.BaseE2eTest;
import com.btxtech.e2e.page.GamePage;
import com.btxtech.e2e.page.LandingPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full Phase 1 (Level 1-9) game flow E2E test.
 *
 * Builder(1) builds: Factory(4), Radar(6), Powerplant(7), Dockyard(11), Tower(21), House(23)
 * Factory(4) fabricates: Builder(1), Harvester(2), Viper(3)
 * Dockyard(11) fabricates: Hydra(12), Transporter(18)
 *
 * Quest titles by ConditionTrigger:
 *   "Deploy unit", "Build" (SYNC_ITEM_CREATED), "Harvest" (HARVEST),
 *   "Destroy" (SYNC_ITEM_KILLED), "Region" (SYNC_ITEM_POSITION), "Sell" (SELL)
 */
class GameStartIT extends BaseE2eTest {

    /**
     * Every spot on the ground this test names, in one place.
     * <p>
     * Where the position is incidental - build a factory somewhere near the base - the test does
     * not name one and lets the placer search. These are the other kind: the position <em>is</em>
     * the quest, and then it belongs in the test as plainly as the item type does.
     * <p>
     * They are an assumption about the terrain, and terrain gets edited - the Phase 2 bridgehead
     * was hand-modelled on PROD and already differs from LOCAL. A spot that turns to water makes
     * this test fail for a reason that has nothing to do with the code under test, so each one
     * carries what it is for: when a step fails, check the spot before the code.
     */
    private static final class Spot {
        /** Noob Island, the player's base area - camera home. */
        static final double[] BASE = {178, 20};
        /** Inside quest 386's coastal strip (PlaceConfig 1797), on land, where the builder waits. */
        static final double[] DOCKYARD_SHORE = {200, 240};
        /** A water tile of the same strip, a dockyard's own terrain type - where it gets built. */
        static final double[] DOCKYARD_SITE = {200, 245};
        /**
         * Fallback for quest 392's region (PlaceConfig 2063) when the bridge does not hand one
         * over. The region's own centroid is open water, and a ship sent there has no land within
         * its reach - this is a shore point instead.
         */
        static final double[] PHASE2_LANDING = {470, 494};
    }

    private static final int BUILDER = 1;
    private static final int HARVESTER = 2;
    private static final int VIPER = 3;
    private static final int FACTORY = 4;
    private static final int RADAR = 6;
    private static final int POWERPLANT = 7;
    private static final int BOT_HYDRA = 10;
    private static final int DOCKYARD = 11;
    private static final int HYDRA = 12;
    private static final int TRANSPORTER = 18;
    private static final int BOT_REFINERY_2 = 24;

    @BeforeEach
    void cleanupGameState() {
        AdminApiClient admin = new AdminApiClient();
        admin.deleteAllHumanBases();
        admin.restartPlanetWarm();
    }

    @Test
    void fullGameFlow() {
        navigateTo("/");
        LandingPage landingPage = new LandingPage(driver);
        GamePage gamePage = landingPage.clickPlayNow();

        deploy(gamePage);
        level1(gamePage);
        level2(gamePage);
        level3(gamePage);
        level4(gamePage);
        level5(gamePage);
        level6(gamePage);
        level7(gamePage);
        level8(gamePage);
        level9(gamePage);
    }

    // ========== Deploy Phase ==========

    private void deploy(GamePage gamePage) {
        gamePage.waitForCanvasPresent();
        assertThat(gamePage.isCanvasDisplayed()).isTrue();
        gamePage.waitForGameReady();
        gamePage.setupErrorCapture();
        assertThat(gamePage.isMainCockpitVisible()).isTrue();

        gamePage.waitForQuestCockpitVisible();
        assertThat(gamePage.getQuestTitle()).isEqualTo("Deploy unit");

        gamePage.waitForBaseItemPlacerActive();

        long itemCountBefore = gamePage.getBaseItemCount();
        gamePage.placeOnFreePosition();

        gamePage.waitForBaseItemCountAbove(itemCountBefore);
        assertThat(gamePage.isBaseItemPlacerActive()).isFalse();
    }

    // ========== Level 1: Build Factory, Fabricate Harvester ==========

    private void level1(GamePage gamePage) {
        gamePage.verifyMainCockpit(1);

        // Quest 358: Build Factory via Builder
        gamePage.waitForQuestProgressContaining("Factory");
        gamePage.selectItemByType(BUILDER);
        gamePage.buildViaBuilder(FACTORY);

        // Quest 359: Fabricate Harvester from Factory
        gamePage.waitForQuestProgressContaining("Harvester");
        gamePage.jsFabricate(FACTORY, HARVESTER);
    }

    // ========== Level 2: Harvest, Fabricate Viper, Kill ==========

    private void level2(GamePage gamePage) {
        gamePage.verifyMainCockpit(2);

        // Quest 363: Harvest 15 Razarion
        gamePage.verifyQuestCockpit("Harvest");
        // One harvest command is not the quest: a harvester stops on an empty field and does not
        // look for the next one, so the order has to be given again - which is what a player does.
        gamePage.waitForQuestCompletedWithRetry(gamePage::jsHarvestNearest, "Harvest", 180);

        // Quest 364: Fabricate Viper from Factory
        gamePage.waitForQuestProgressContaining("Viper");
        gamePage.jsFabricate(FACTORY, VIPER);

        // Quest 365: Kill 1 enemy unit (avoid killing Refinery 2 needed for level 5)
        gamePage.verifyQuestCockpit("Destroy");
        gamePage.waitForOwnItemCountByType(VIPER, 1);
        gamePage.jsAttackEnemyExcludingTypeUntilDone(BOT_REFINERY_2);
    }

    // ========== Level 3: Build Radar, Build Powerplant ==========

    private void level3(GamePage gamePage) {
        gamePage.verifyMainCockpit(3);

        // Quest 361: Build Radar via Builder
        gamePage.waitForQuestProgressContaining("Radar");
        gamePage.selectItemByType(BUILDER);
        gamePage.buildViaBuilder(RADAR);

        // Quest 362: Build Powerplant via Builder
        gamePage.waitForQuestProgressContaining("Powerplant");
        gamePage.selectItemByType(BUILDER);
        gamePage.buildViaBuilder(POWERPLANT);
    }

    // ========== Level 4: Harvest 30, Fabricate 3 Vipers ==========

    private void level4(GamePage gamePage) {
        gamePage.verifyMainCockpit(4);

        // Quest 366: Harvest 30 Razarion
        gamePage.verifyQuestCockpit("Harvest");
        gamePage.waitForQuestCompletedWithRetry(gamePage::jsHarvestNearest, "Harvest", 180);

        // Quest 369: Fabricate 3 Vipers from Factory
        gamePage.waitForQuestProgressContaining("Viper");
        // Counted by the quest, not by the renderer: a finished viper drives off and stops being
        // rendered, and the quest counts existing ones too, so an item count says neither how many
        // were built nor how many are still needed.
        gamePage.waitForQuestCompletedWithRetry(() -> gamePage.jsFabricate(FACTORY, VIPER), "Build", 180);
    }

    // ========== Level 5: Kill Bot Refinery 2 ==========

    private void level5(GamePage gamePage) {
        gamePage.verifyMainCockpit(5);

        // Quest 379: Kill (Bot) Refinery 2
        gamePage.verifyQuestCockpit("Destroy");

        // Bot Refinery 2 is far from base (~165,125). Build up a large attack force first.
        gamePage.jsHarvestNearest();
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Fabricate extra vipers for the assault (need ~6 total)
        long viperCount = gamePage.getOwnItemCountByType(VIPER);
        int targetVipers = 6;
        for (long i = viperCount; i < targetVipers; i++) {
            gamePage.jsFabricate(FACTORY, VIPER);
            gamePage.waitForOwnItemCountByType(VIPER, i + 1);
        }

        gamePage.jsAttackEnemyOfTypeUntilDone(BOT_REFINERY_2);

        // Move camera back to base for level 6
        gamePage.jsMoveCamera(Spot.BASE[0], Spot.BASE[1]);
    }

    // ========== Level 6: Dockyard in region ==========

    private void level6(GamePage gamePage) {
        gamePage.verifyMainCockpit(6);

        // Quest 386: Build Dockyard in quest region
        // Polygon region: coastal strip, centroid ~(200,230), safe interior point ~(200,245)
        gamePage.verifyQuestCockpit("Region");

        // The fight of level 5 can cost the builder, and without one this level cannot start at
        // all. Rebuilding it from the factory is what the quest expects of a player too.
        if (gamePage.jsOwnItemsOfType(BUILDER).isEmpty()) {
            System.out.println("[E2E] no builder left after the fight, building one");
            gamePage.waitUntil(() -> {
                gamePage.jsFabricate(FACTORY, BUILDER);
                return !gamePage.jsOwnItemsOfType(BUILDER).isEmpty();
            }, 120, "a new builder");
        }
        int builderId = gamePage.jsGetOwnItemId(BUILDER);
        System.out.println("[E2E] Builder ID: " + builderId);

        // Walk there first, then build. A builder does walk to its own site, but the site is on
        // water and it has to find land within build range of it from wherever it starts; sending
        // it to the shore first is what made this level pass, and dropping that was a regression
        // of this test, not of the game. Best effort: if it is slow, the build order still brings
        // it the rest of the way.
        gamePage.jsMoveById(builderId, Spot.DOCKYARD_SHORE[0], Spot.DOCKYARD_SHORE[1]);
        try {
            gamePage.waitUntil(() -> gamePage.isNear(gamePage.jsPositionOfType(BUILDER),
                    Spot.DOCKYARD_SHORE[0], Spot.DOCKYARD_SHORE[1], 30), 120, "the builder at the shore");
        } catch (RuntimeException e) {
            System.out.println("[E2E] builder still on its way, ordering the build anyway");
        }
        gamePage.jsMoveCamera(Spot.DOCKYARD_SHORE[0], Spot.DOCKYARD_SHORE[1]);
        gamePage.jsBuildById(builderId, DOCKYARD, Spot.DOCKYARD_SITE[0], Spot.DOCKYARD_SITE[1]);
        // Given once, then left alone. Repeating it every ten seconds looked like robustness and
        // was the opposite: a new command stops the current job, so the builder kept restarting a
        // walk of some two hundred units and never arrived. The engine brings it there.
        gamePage.waitForQuestCompletedWithRetry(() -> {}, "Region", 180);
    }

    // ========== Level 7: Fabricate Hydra, Kill Bot Hydra ==========

    private void level7(GamePage gamePage) {
        gamePage.verifyMainCockpit(7);

        // Quest 387: Fabricate Hydra from Dockyard
        gamePage.verifyQuestCockpit("Build");
        // Same as the vipers: one order is not the quest. The dockyard may still be finishing,
        // the queue may have swallowed it, and only the quest knows when it is done.
        gamePage.waitForQuestCompletedWithRetry(() -> gamePage.jsFabricate(DOCKYARD, HYDRA), "Build", 180);

        // Quest 388: Kill (Bot) Hydra. The water fight: hydra against hydra, both of them far from
        // the base and usually off screen, so the attack order is given by id.
        gamePage.verifyQuestCockpit("Destroy");
        gamePage.waitForQuestCompletedWithRetry(() -> {
            // One hydra against the twenty-five the bot keeps in the water loses as often as it
            // wins, and a dead attacker makes the order a no-op forever. Rebuild, then attack -
            // which is what the quest expects of a player too.
            if (gamePage.jsOwnItemsOfType(HYDRA).size() < 4) {
                gamePage.jsFabricate(DOCKYARD, HYDRA);
            }
            gamePage.jsAttackWithType(HYDRA, BOT_HYDRA);
        }, "Destroy", 240);
    }

    // ========== Level 8: Fabricate Transporter, Builder on region ==========

    private void level8(GamePage gamePage) {
        gamePage.verifyMainCockpit(8);

        // Quest 389: Fabricate Transporter from Dockyard
        gamePage.verifyQuestCockpit("Build");
        gamePage.waitForQuestCompletedWithRetry(() -> gamePage.jsFabricate(DOCKYARD, TRANSPORTER), "Build", 180);
        // Not the rendered count: after the water fight the camera is out at sea, and the fresh
        // transporter sits at the dockyard.
        gamePage.waitUntil(() -> !gamePage.jsOwnItemsOfType(TRANSPORTER).isEmpty(), 60, "the transporter to exist");

        // Quest 392: Move Builder to Phase 2 region. Four separate things have to work, and the
        // player has to find all four: load the builder, sail across, press Unload, and place the
        // builder on land within the ship's range. Each one is its own step here, so a failure
        // says which of them it was.
        gamePage.verifyQuestCockpit("Region");
        double[] regionCenter = gamePage.getQuestRegionCenter();
        double destX = regionCenter != null ? regionCenter[0] : Spot.PHASE2_LANDING[0];
        double destY = regionCenter != null ? regionCenter[1] : Spot.PHASE2_LANDING[1];
        System.out.println("[E2E] quest 392 region at " + destX + "," + destY);

        gamePage.jsLoadIntoTransporter(BUILDER);
        gamePage.waitUntil(() -> gamePage.isTransporterLoaded(BUILDER), 60,
                "the builder to be inside the transporter");

        gamePage.jsMoveItemsOfType(TRANSPORTER, destX, destY);
        gamePage.waitUntil(() -> gamePage.isNear(gamePage.jsPositionOfType(TRANSPORTER), destX, destY, 20), 180,
                "the transporter to reach the region");

        // The unload button opens the placer; the spot still has to be picked, on land and within
        // the ship's range - and an unload that is out of range is dropped without a word.
        gamePage.jsMoveCamera(destX, destY);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        System.out.println("[E2E] container range of the transporter: " + gamePage.jsContainerRange(TRANSPORTER));
        gamePage.jsUnloadTransporter();
        gamePage.waitForBaseItemPlacerActive();
        gamePage.placeOnFreePosition();

        gamePage.waitForQuestCompletedWithRetry(() -> {
            // Says whether the builder ever came out: an unload the engine refuses - out of the
            // ship's range, or onto something that is not land - is dropped without a word, and
            // from the outside that looks exactly like a placement nobody made.
            System.out.println("[E2E] unload attempt: builders on the planet=" + gamePage.jsOwnItemsOfType(BUILDER)
                    + " transporter=" + gamePage.jsOwnItemsOfType(TRANSPORTER));
            gamePage.jsUnloadTransporter();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            if (gamePage.isBaseItemPlacerActive()) {
                gamePage.placeOnFreePosition();
            } else {
                System.out.println("[E2E] unload attempt: the placer did not open");
            }
        }, "Region", 120);
    }

    // ========== Level 9: Sell, Relocate, Sell ==========

    private void level9(GamePage gamePage) {
        gamePage.verifyMainCockpit(9);

        // Move camera to builder's location (Phase 2 region after level 8 transport)
        double[] builderPos = gamePage.jsGetOwnItemPosition(BUILDER);
        if (builderPos != null) {
            gamePage.jsMoveCamera(builderPos[0], builderPos[1]);
        }

        // Quest 393: Sell Factory
        gamePage.verifyQuestCockpit("Sell");
        gamePage.jsSellItemsOfType(FACTORY);

        // Quest 395: Build Factory in Phase 2 start region
        gamePage.waitForQuestProgressContaining("Factory");
        gamePage.selectItemByType(BUILDER);
        gamePage.buildViaBuilder(FACTORY);

        // Quest 396: Build Radar + Powerplant in region
        gamePage.waitForQuestProgressContaining("Radar");
        gamePage.selectItemByType(BUILDER);
        gamePage.buildViaBuilder(RADAR);
        gamePage.selectItemByType(BUILDER);
        gamePage.buildViaBuilder(POWERPLANT);

        // Quest 400: Harvester x2 + Viper x6
        gamePage.waitForQuestProgressContaining("Harvester");
        long harvestersBefore = gamePage.getOwnItemCountByType(HARVESTER);
        for (int i = 0; i < 2; i++) {
            gamePage.jsFabricate(FACTORY, HARVESTER);
            gamePage.waitForOwnItemCountByType(HARVESTER, harvestersBefore + i + 1);
        }
        long vipersBefore9 = gamePage.getOwnItemCountByType(VIPER);
        for (int i = 0; i < 6; i++) {
            gamePage.jsFabricate(FACTORY, VIPER);
            gamePage.waitForOwnItemCountByType(VIPER, vipersBefore9 + i + 1);
        }

        // Quest 401: Sell Dockyard
        gamePage.verifyQuestCockpit("Sell");
        gamePage.jsSellItemsOfType(DOCKYARD);
        gamePage.waitForQuestCompleted();
    }
}
