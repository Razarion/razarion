package com.btxtech.e2e.page;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class GamePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By CANVAS = By.cssSelector("canvas.canvas");
    private static final By LOADING_OVERLAY = By.cssSelector("div.cover-panel");
    private static final By MAIN_COCKPIT = By.cssSelector("main-cockpit");
    private static final By QUEST_COCKPIT = By.cssSelector("quest-cockpit");
    private static final By QUEST_TITLE = By.cssSelector("quest-cockpit .font-semibold.text-3xl");
    private static final By ITEM_COCKPIT = By.cssSelector("item-cockpit");
    private static final By BUILD_BUTTON = By.cssSelector("button.item-cockpit-buildup-button:enabled");

    public GamePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    public void waitForCanvasPresent() {
        wait.until(ExpectedConditions.presenceOfElementLocated(CANVAS));
    }

    public void waitForGameReady() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_OVERLAY));
    }

    public boolean isCanvasDisplayed() {
        return driver.findElement(CANVAS).isDisplayed();
    }

    public boolean isMainCockpitVisible() {
        return driver.findElement(MAIN_COCKPIT).isDisplayed();
    }

    public void waitForQuestCockpitVisible() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(QUEST_COCKPIT));
    }

    public boolean isQuestCockpitVisible() {
        return driver.findElement(QUEST_COCKPIT).isDisplayed();
    }

    public String getQuestTitle() {
        return driver.findElement(QUEST_TITLE).getText();
    }

    public void waitForBaseItemPlacerActive() {
        wait.until(d -> executeScript(
                "return window.gwtAngularFacade " +
                "&& window.gwtAngularFacade.babylonRenderServiceAccess " +
                "&& window.gwtAngularFacade.babylonRenderServiceAccess.baseItemPlacerActive === true;"
        ));
    }

    public boolean isBaseItemPlacerActive() {
        return Boolean.TRUE.equals(executeScript(
                "return window.gwtAngularFacade.babylonRenderServiceAccess.baseItemPlacerActive;"
        ));
    }

    public boolean isBaseItemPlacerMeshRendered() {
        return Boolean.TRUE.equals(executeScript(
                "var scene = window.gwtAngularFacade.babylonRenderServiceAccess.getScene();" +
                "var mesh = scene.getMeshByName('Base Item Placer');" +
                "return mesh !== null && !mesh.isDisposed();"
        ));
    }

    public void clickCanvas() {
        WebElement canvas = driver.findElement(CANVAS);
        new Actions(driver).moveToElement(canvas).click().perform();
    }

    public void clickCanvasAt(int offsetX, int offsetY) {
        WebElement canvas = driver.findElement(CANVAS);
        new Actions(driver).moveToElement(canvas, offsetX, offsetY).click().perform();
    }

    public void placeOnFreePosition() {
        int[][] offsets = {{0, 0}, {150, 0}, {-150, 0}, {0, 150}, {0, -150}, {150, 150}, {-150, -150}};
        for (int[] offset : offsets) {
            clickCanvasAt(offset[0], offset[1]);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(3)).until(d -> isBaseItemPlacerInactive());
                return;
            } catch (Exception e) {
                // Terrain not free, try next position
            }
        }
        throw new RuntimeException("Could not find free terrain for placement after trying " + offsets.length + " positions");
    }

    public boolean hasActiveSpawnParticles() {
        return Boolean.TRUE.equals(executeScript(
                "var scene = window.gwtAngularFacade.babylonRenderServiceAccess.getScene();" +
                "var ps = scene.particleSystems || [];" +
                "for (var i = 0; i < ps.length; i++) { if (ps[i].isStarted()) return true; }" +
                "return false;"
        ));
    }

    public void waitForSpawnParticle() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> hasActiveSpawnParticles());
    }

    public long getBaseItemCount() {
        Object result = executeScript(
                "var scene = window.gwtAngularFacade.babylonRenderServiceAccess.getScene();" +
                "var container = scene.getTransformNodeByName('Base items');" +
                "if (!container) return 0;" +
                "return container.getChildren().filter(function(c) {" +
                "  return c.name && c.name.indexOf(\"'\") !== -1;" +
                "}).length;"
        );
        return result instanceof Number ? ((Number) result).longValue() : 0;
    }

    public void waitForBaseItemCountAbove(long previousCount) {
        wait.until(d -> getBaseItemCount() > previousCount);
    }

    public boolean isBaseItemPlacerInactive() {
        return Boolean.TRUE.equals(executeScript(
                "return window.gwtAngularFacade.babylonRenderServiceAccess.baseItemPlacerActive === false;"
        ));
    }

    public void waitForBaseItemPlacerInactive() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> isBaseItemPlacerInactive());
    }

    public void waitForItemCockpitVisible() {
        wait.until(d -> {
            try {
                String name = getSelectedItemName();
                return name != null && !name.isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
    }

    public String getSelectedItemName() {
        Object result = executeScript(
                "var cockpit = document.querySelector('item-cockpit');" +
                "if (!cockpit) return '';" +
                "var divs = cockpit.querySelectorAll('div');" +
                "for (var i = 0; i < divs.length; i++) {" +
                "  if (divs[i].style && divs[i].style.fontSize === 'larger') {" +
                "    return divs[i].textContent.trim();" +
                "  }" +
                "}" +
                "return '';"
        );
        return result != null ? result.toString() : "";
    }

    public String getCursorStyle() {
        Object result = executeScript(
                "return getComputedStyle(document.querySelector('.game-main')).getPropertyValue('--cursor').trim();"
        );
        return result != null ? result.toString() : "";
    }

    public boolean isMoveCursor() {
        return getCursorStyle().contains("move.png");
    }

    public void hoverCanvas() {
        WebElement canvas = driver.findElement(CANVAS);
        new Actions(driver).moveToElement(canvas).perform();
    }

    public void clickFirstBuildButton() {
        WebElement button = driver.findElement(BUILD_BUTTON);
        new Actions(driver).moveToElement(button).click().perform();
    }

    public boolean hasBuildButtons() {
        return !driver.findElements(BUILD_BUTTON).isEmpty();
    }

    private Object executeScript(String script) {
        return ((JavascriptExecutor) driver).executeScript(script);
    }
}
