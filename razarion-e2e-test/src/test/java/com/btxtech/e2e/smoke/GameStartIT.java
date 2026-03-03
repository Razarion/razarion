package com.btxtech.e2e.smoke;

import com.btxtech.e2e.base.BaseE2eTest;
import com.btxtech.e2e.page.GamePage;
import com.btxtech.e2e.page.LandingPage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameStartIT extends BaseE2eTest {

    @Test
    void fullGameFlow() {
        // Start game from landing page
        navigateTo("/");
        LandingPage landingPage = new LandingPage(driver);
        GamePage gamePage = landingPage.clickPlayNow();

        // Verify canvas and game loading
        gamePage.waitForCanvasPresent();
        assertThat(gamePage.isCanvasDisplayed()).isTrue();
        gamePage.waitForGameReady();
        assertThat(gamePage.isMainCockpitVisible()).isTrue();

        // Verify quest cockpit shows "Deploy unit"
        gamePage.waitForQuestCockpitVisible();
        assertThat(gamePage.isQuestCockpitVisible()).isTrue();
        assertThat(gamePage.getQuestTitle()).isEqualTo("Deploy unit");

        // Verify BaseItemPlacer is active and rendered
        gamePage.waitForBaseItemPlacerActive();
        assertThat(gamePage.isBaseItemPlacerActive()).isTrue();
        assertThat(gamePage.isBaseItemPlacerMeshRendered()).isTrue();

        // Place builder unit on free terrain
        long itemCountBefore = gamePage.getBaseItemCount();
        gamePage.placeOnFreePosition();

        // Verify item creation
        gamePage.waitForBaseItemCountAbove(itemCountBefore);
        assertThat(gamePage.getBaseItemCount()).isGreaterThan(itemCountBefore);
        assertThat(gamePage.isBaseItemPlacerActive()).isFalse();

        // Select the builder by clicking on it
        gamePage.clickCanvas();
        gamePage.waitForItemCockpitVisible();
        assertThat(gamePage.getSelectedItemName()).isNotEmpty();

        // Verify move cursor when hovering terrain
        gamePage.hoverCanvas();
        assertThat(gamePage.isMoveCursor()).isTrue();

        // Click build button to start factory placement
        assertThat(gamePage.hasBuildButtons()).isTrue();
        long itemCountBeforeFactory = gamePage.getBaseItemCount();
        gamePage.clickFirstBuildButton();

        // Verify placer activates for factory
        gamePage.waitForBaseItemPlacerActive();
        assertThat(gamePage.isBaseItemPlacerMeshRendered()).isTrue();

        // Place the factory on free terrain
        gamePage.placeOnFreePosition();
        gamePage.waitForBaseItemCountAbove(itemCountBeforeFactory);
        assertThat(gamePage.getBaseItemCount()).isGreaterThan(itemCountBeforeFactory);
    }
}
