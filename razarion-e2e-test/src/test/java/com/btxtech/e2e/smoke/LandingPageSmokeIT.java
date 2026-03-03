package com.btxtech.e2e.smoke;

import com.btxtech.e2e.base.BaseE2eTest;
import com.btxtech.e2e.page.LandingPage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LandingPageSmokeIT extends BaseE2eTest {

    @Test
    void landingPageLoadsWithCorrectTitle() {
        navigateTo("/");
        LandingPage landingPage = new LandingPage(driver);

        assertThat(landingPage.getTitle()).containsIgnoringCase("Razarion");
    }

    @Test
    void landingPageDisplaysLogoAndPlayButton() {
        navigateTo("/");
        LandingPage landingPage = new LandingPage(driver);

        assertThat(landingPage.getLogoText()).isEqualTo("RAZARION");
        assertThat(landingPage.isPlayNowButtonDisplayed()).isTrue();
    }
}
