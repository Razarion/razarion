package com.btxtech.e2e.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LandingPage {

    private final WebDriver driver;

    private static final By LOGO = By.cssSelector("h1.logo");
    private static final By TAGLINE = By.cssSelector("p.tagline");
    private static final By PLAY_NOW_BUTTON = By.cssSelector("button.button");

    public LandingPage(WebDriver driver) {
        this.driver = driver;
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public String getLogoText() {
        return driver.findElement(LOGO).getText();
    }

    public String getTaglineText() {
        return driver.findElement(TAGLINE).getText();
    }

    public boolean isPlayNowButtonDisplayed() {
        return driver.findElement(PLAY_NOW_BUTTON).isDisplayed();
    }

    public GamePage clickPlayNow() {
        driver.findElement(PLAY_NOW_BUTTON).click();
        return new GamePage(driver);
    }
}
