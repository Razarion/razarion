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
        gamePage.jsHarvestNearest();

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
        gamePage.jsHarvestNearest();

        // Quest 369: Fabricate 3 Vipers from Factory
        gamePage.waitForQuestProgressContaining("Viper");
        long vipersBefore = gamePage.getOwnItemCountByType(VIPER);
        for (int i = 0; i < 3; i++) {
            gamePage.jsFabricate(FACTORY, VIPER);
            gamePage.waitForOwnItemCountByType(VIPER, vipersBefore + i + 1);
        }
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
        gamePage.jsMoveCamera(178, 20);
    }

    // ========== Level 6: Dockyard in region ==========

    private void level6(GamePage gamePage) {
        gamePage.verifyMainCockpit(6);

        // Quest 386: Build Dockyard in quest region (use quest region API to find location)
        gamePage.verifyQuestCockpit("Region");
        gamePage.selectItemByType(BUILDER);
        gamePage.buildViaBuilderInQuestRegion(DOCKYARD);
    }

    // ========== Level 7: Fabricate Hydra, Kill Bot Hydra ==========

    private void level7(GamePage gamePage) {
        gamePage.verifyMainCockpit(7);

        // Quest 387: Fabricate Hydra from Dockyard
        gamePage.verifyQuestCockpit("Build");
        gamePage.jsFabricate(DOCKYARD, HYDRA);

        // Quest 388: Kill (Bot) Hydra
        gamePage.verifyQuestCockpit("Destroy");
        gamePage.jsAttackEnemyOfTypeUntilDone(BOT_HYDRA);
    }

    // ========== Level 8: Fabricate Transporter, Builder on region ==========

    private void level8(GamePage gamePage) {
        gamePage.verifyMainCockpit(8);

        // Quest 389: Fabricate Transporter from Dockyard
        gamePage.verifyQuestCockpit("Build");
        gamePage.jsFabricate(DOCKYARD, TRANSPORTER);

        // Quest 392: Move Builder to Phase 2 region
        gamePage.verifyQuestCockpit("Region");
        gamePage.jsLoadIntoTransporter(BUILDER);
        gamePage.jsMoveItemsOfType(TRANSPORTER, 200, 500);
    }

    // ========== Level 9: Sell, Relocate, Sell ==========

    private void level9(GamePage gamePage) {
        gamePage.verifyMainCockpit(9);

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

        // Quest 400: Harvester x2 + Viper x6 in region
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
