package com.btxtech.e2e.page;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GamePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    /** Dockyard(11) fabricates it; it is the only container in the game. */
    private static final int TRANSPORTER_TYPE_ID = 18;

    private static final By CANVAS = By.cssSelector("canvas.canvas");
    private static final By LOADING_OVERLAY = By.cssSelector("div.cover-panel");
    private static final By MAIN_COCKPIT = By.cssSelector("main-cockpit");
    private static final By QUEST_COCKPIT = By.cssSelector("quest-cockpit");
    private static final By QUEST_TITLE = By.cssSelector("quest-cockpit .quest-title");
    private static final By ITEM_COCKPIT = By.cssSelector("item-cockpit");
    /**
     * The build buttons, in both layouts the cockpit has: the HUD grid it renders today and the
     * carousel it still falls back to.
     * <p>
     * Not :enabled - the buttons are deliberately never disabled. A disabled button swallows the
     * click and the reason it cannot be pressed goes with it, so the cockpit keeps them pressable
     * and says why instead. That makes :enabled true for every one of them, which is how this test
     * came to click on buttons that could not build.
     */
    private static final String BUILDABLE = "button.hud-build-btn:not([aria-disabled='true'])";
    private static final String BUILDABLE_CAROUSEL = "button.item-cockpit-buildup-button:not([aria-disabled='true'])";
    private static final By BUILD_BUTTON = By.cssSelector(BUILDABLE + ", " + BUILDABLE_CAROUSEL);

    /** The build button for one item type, buildable right now. */
    private static By buildableButton(int itemTypeId) {
        String cell = "[data-item-type-id='" + itemTypeId + "'] ";
        return By.cssSelector(cell + BUILDABLE + ", " + cell + BUILDABLE_CAROUSEL);
    }
    // The label is a property binding ([label]="sellArmed ? 'Sell?' : '$'"), so it is not in the
    // DOM as an attribute: match the rendered text instead.
    private static final By SELL_BUTTON = By.xpath("//item-cockpit//button[normalize-space(.)='$' or normalize-space(.)='Sell?']");
    private static final By QUEST_PROGRESS_ROW = By.cssSelector("quest-cockpit .flex.flex-row .flex:last-child");
    private static final By QUEST_DONE_ICON = By.cssSelector("quest-cockpit .pi-check-circle");
    private static final By LEVEL_BADGE = By.cssSelector("main-cockpit .hud-xp-badge");

    public GamePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    // ========== Canvas & Loading ==========

    public void waitForCanvasPresent() {
        wait.until(ExpectedConditions.presenceOfElementLocated(CANVAS));
    }

    public void waitForGameReady() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_OVERLAY));
    }

    public boolean isCanvasDisplayed() {
        return driver.findElement(CANVAS).isDisplayed();
    }

    // ========== Main Cockpit ==========

    public boolean isMainCockpitVisible() {
        return driver.findElement(MAIN_COCKPIT).isDisplayed();
    }

    public int getLevelNumber() {
        String text = driver.findElement(LEVEL_BADGE).getText().trim();
        // Text is "Level: X"
        return Integer.parseInt(text.replaceAll("\\D+", ""));
    }

    public void waitForLevel(int expectedLevel) {
        final long[] lastLog = {0};
        wait.until(d -> {
            try {
                int current = getLevelNumber();
                if (current == expectedLevel) return true;
                long now = System.currentTimeMillis();
                if (now - lastLog[0] > 5000) {
                    System.out.println("[E2E] waitForLevel(" + expectedLevel + ") current level: " + current);
                    lastLog[0] = now;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ========== Quest Cockpit ==========

    public void waitForQuestCockpitVisible() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(QUEST_COCKPIT));
    }

    public boolean isQuestCockpitVisible() {
        return driver.findElement(QUEST_COCKPIT).isDisplayed();
    }

    public String getQuestTitle() {
        return driver.findElement(QUEST_TITLE).getText().trim();
    }

    public void waitForQuestTitle(String expectedTitle) {
        wait.until(d -> {
            try {
                return getQuestTitle().equals(expectedTitle);
            } catch (Exception e) {
                return false;
            }
        });
    }

    public List<String> getQuestProgressTexts() {
        List<String> texts = new ArrayList<>();
        List<WebElement> rows = driver.findElements(QUEST_PROGRESS_ROW);
        for (WebElement row : rows) {
            texts.add(row.getText().trim());
        }
        return texts;
    }

    public boolean isQuestProgressAllDone() {
        List<WebElement> doneIcons = driver.findElements(QUEST_DONE_ICON);
        List<WebElement> rows = driver.findElements(QUEST_PROGRESS_ROW);
        return !rows.isEmpty() && doneIcons.size() >= rows.size();
    }

    public void waitForQuestCompleted() {
        wait.until(d -> isQuestProgressAllDone());
    }

    /**
     * Waits until quest progress text contains the given substring.
     * Useful to distinguish quests with the same title.
     */
    public void waitForQuestProgressContaining(String text) {
        final long[] lastLog = {0};
        wait.until(d -> {
            try {
                executeScript("if (window.__e2eAppRef) { window.__e2eAppRef.tick(); }");
                List<String> progress = getQuestProgressTexts();
                for (String p : progress) {
                    if (p.contains(text)) return true;
                }
                long now = System.currentTimeMillis();
                if (now - lastLog[0] > 5000) {
                    System.out.println("[E2E] waitForQuestProgressContaining('" + text + "') current: " + progress);
                    lastLog[0] = now;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        });
    }

    public void waitForNextQuest(String previousTitle) {
        wait.until(d -> {
            try {
                String title = getQuestTitle();
                return !title.equals(previousTitle) || isQuestProgressAllDone();
            } catch (Exception e) {
                return false;
            }
        });
    }

    // ========== BaseItemPlacer ==========

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

    public boolean isBaseItemPlacerInactive() {
        return Boolean.TRUE.equals(executeScript(
                "return window.gwtAngularFacade.babylonRenderServiceAccess.baseItemPlacerActive === false;"
        ));
    }

    public void waitForBaseItemPlacerInactive() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> isBaseItemPlacerInactive());
    }

    public void placeOnFreePosition() {
        // Try a wide grid of positions across the canvas, spiraling outward
        List<int[]> offsets = new ArrayList<>();
        offsets.add(new int[]{0, 0});
        // Fine rings first. Eighty pixels is already several metres of ground, and an unload has
        // only the band of land within the ship's reach - a grid that starts at 80 can step over
        // it entirely and report that nothing is placeable anywhere.
        for (int radius = 20; radius < 80; radius += 20) {
            for (int angle = 0; angle < 360; angle += 30) {
                offsets.add(new int[]{(int) (radius * Math.cos(Math.toRadians(angle))),
                        (int) (radius * Math.sin(Math.toRadians(angle)))});
            }
        }
        for (int radius = 80; radius <= 500; radius += 80) {
            for (int angle = 0; angle < 360; angle += 30) {
                int x = (int) (radius * Math.cos(Math.toRadians(angle)));
                int y = (int) (radius * Math.sin(Math.toRadians(angle)));
                offsets.add(new int[]{x, y});
            }
        }
        for (int[] offset : offsets) {
            try {
                clickCanvasAt(offset[0], offset[1]);
                new WebDriverWait(driver, Duration.ofSeconds(1)).until(d -> isBaseItemPlacerInactive());
                // How far from the middle of the screen the accepted spot was. For an unload that
                // is the distance from the ship, which is what decides whether the engine accepts
                // it - and the placer itself knows nothing about that range.
                System.out.println("[E2E] placed at screen offset " + offset[0] + "/" + offset[1]
                        + " (" + (int) Math.hypot(offset[0], offset[1]) + " px from the centre)");
                return;
            } catch (Exception e) {
                // Terrain not free, out of bounds, or not in quest region — try next
            }
        }
        // Where the tried points actually were on the ground. A placement that is refused
        // everywhere is either a spot that really is unusable or a grid that misses the usable
        // band, and only the ground coordinates tell the two apart.
        System.out.println("[E2E] nothing placeable. Ground under the canvas centre: " + groundAt(0, 0)
                + ", at +80/0: " + groundAt(80, 0) + ", at 0/+80: " + groundAt(0, 80));
        throw new RuntimeException("Could not find free terrain for placement after trying " + offsets.size() + " positions");
    }

    /**
     * Gets the quest region center from the active quest's PlaceConfig via WASM bridge.
     * Returns [x, y] or null if no place config found.
     */
    @SuppressWarnings("unchecked")
    public double[] getQuestRegionCenter() {
        Object result = executeScript(
                "var questVis = window.gwtAngularFacade.inGameQuestVisualizationService;" +
                "if (!questVis || !questVis.getActiveQuestPlaceConfig) return 'NO_BRIDGE';" +
                "var pc = questVis.getActiveQuestPlaceConfig();" +
                "if (!pc) return 'NO_PLACE_CONFIG';" +
                "var pos = pc.getPosition();" +
                "if (pos) return [pos.getX(), pos.getY()];" +
                "var poly = pc.getPolygon2D();" +
                "if (poly) {" +
                "  var corners = poly.toCornersAngular();" +
                "  if (corners && corners.length > 0) {" +
                "    var cx = 0, cy = 0;" +
                "    for (var i = 0; i < corners.length; i++) {" +
                "      cx += corners[i].getX(); cy += corners[i].getY();" +
                "    }" +
                "    return [cx / corners.length, cy / corners.length];" +
                "  }" +
                "}" +
                "return 'PLACE_CONFIG_EMPTY';"
        );
        if (result instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) result;
            double[] center = new double[]{((Number) list.get(0)).doubleValue(), ((Number) list.get(1)).doubleValue()};
            System.out.println("[E2E] Quest region center (bridge): " + center[0] + ", " + center[1]);
            return center;
        }
        System.out.println("[E2E] Quest region bridge debug: " + result);
        return null;
    }

    /**
     * Places building at the quest region using buildCmd with coordinates.
     * Falls back to camera grid + canvas click if bridge unavailable.
     */
    public void placeInQuestRegion() {
        // Fallback: search camera positions + canvas click (bridge path is in buildViaBuilderInQuestRegion)
        double[][] cameraPositions = {
                {150, 180}, {180, 180}, {120, 180},
                {150, 150}, {180, 150}, {120, 150},
                {150, 220}, {180, 220}, {120, 220},
                {150, 250}, {180, 250}, {100, 200},
                {200, 180}, {200, 150}, {80, 180},
                {178, 100}, {178, 50}, {150, 100},
                {250, 180}, {250, 150}, {250, 200},
                {100, 150}, {100, 100}, {50, 180},
        };
        for (double[] pos : cameraPositions) {
            jsMoveCamera(pos[0], pos[1]);
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            try {
                placeOnFreePosition();
                return;
            } catch (RuntimeException e) {
                System.out.println("[E2E] Placement failed at camera (" + pos[0] + "," + pos[1] + ")");
            }
        }
        throw new RuntimeException("Could not place building in quest region after exhaustive search");
    }

    /**
     * Sends buildCmd directly to place a building at specific terrain coordinates.
     */
    public void jsBuildAtPosition(int itemTypeId, double x, double y) {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var builderId = null;" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getBuilderType() != null) {" +
                "    builderId = items[i].getId();" +
                "    break;" +
                "  }" +
                "}" +
                "if (!builderId) return 'no builder found';" +
                "try {" +
                "  gameCmd.buildCmd(builderId, " + x + ", " + y + ", " + itemTypeId + ");" +
                "  return 'buildCmd sent: builder=' + builderId + ' at ' + " + x + " + ',' + " + y + " + ' type=' + " + itemTypeId + ";" +
                "} catch(e) {" +
                "  return 'buildCmd error: ' + e.message;" +
                "}"
        );
        System.out.println("[E2E] jsBuildAtPosition: " + result);
    }

    // ========== Item Cockpit ==========

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

    // ========== Build Buttons ==========

    public boolean hasBuildButtons() {
        return !driver.findElements(BUILD_BUTTON).isEmpty();
    }

    public void clickFirstBuildButton() {
        WebElement button = driver.findElement(BUILD_BUTTON);
        new Actions(driver).moveToElement(button).click().perform();
    }

    public void clickBuildButtonForItemType(int itemTypeId) {
        By selector = buildableButton(itemTypeId);
        WebElement button = driver.findElement(selector);
        new Actions(driver).moveToElement(button).click().perform();
    }

    public boolean hasBuildButtonForItemType(int itemTypeId) {
        By selector = buildableButton(itemTypeId);
        return !driver.findElements(selector).isEmpty();
    }

    public void waitForBuildButtonForItemType(int itemTypeId) {
        By enabledSelector = buildableButton(itemTypeId);
        try {
            wait.until(d -> {
                if (!d.findElements(enabledSelector).isEmpty()) return true;
                // Periodically trigger Angular change detection to ensure cockpit renders
                executeScript(
                        "if (window.__e2eAppRef) { window.__e2eAppRef.tick(); }"
                );
                return false;
            });
        } catch (RuntimeException e) {
            // A selector that no longer matches and a cockpit that never opened fail identically
            // from here. Say which one it was, with what is actually in the page.
            System.out.println("[E2E] no build button for type " + itemTypeId + ". " + describeCockpit());
            throw e;
        }
    }

    /** What the item cockpit currently offers, for a build button that could not be found. */
    private String describeCockpit() {
        try {
            return String.valueOf(executeScript(
                    "var cockpit = document.querySelector('item-cockpit');" +
                    "if (!cockpit) { return 'no item-cockpit element'; }" +
                    "var tiles = [].map.call(cockpit.querySelectorAll('[data-item-type-id]'), function (tile) {" +
                    "  var button = tile.querySelector('button');" +
                    "  return tile.getAttribute('data-item-type-id')" +
                    "    + (button ? '(' + button.className + (button.disabled ? ',disabled' : '')" +
                    "       + ',aria-disabled=' + button.getAttribute('aria-disabled') + ')' : '(no button)');" +
                    "});" +
                    "return 'visible=' + (cockpit.offsetParent !== null)" +
                    "  + ' tiles=[' + tiles.join(' ') + ']'" +
                    "  + ' buttons=' + cockpit.querySelectorAll('button.hud-build-btn, button.item-cockpit-buildup-button').length;"));
        } catch (RuntimeException e) {
            return "cockpit not readable: " + e.getMessage();
        }
    }

    // ========== Sell Button ==========

    public void clickSellButton() {
        WebElement button = driver.findElement(SELL_BUTTON);
        new Actions(driver).moveToElement(button).click().perform();
    }

    public boolean hasSellButton() {
        return !driver.findElements(SELL_BUTTON).isEmpty();
    }

    public void waitForSellButton() {
        wait.until(ExpectedConditions.presenceOfElementLocated(SELL_BUTTON));
    }

    // ========== Canvas Click & Hover ==========

    public void clickCanvas() {
        WebElement canvas = driver.findElement(CANVAS);
        new Actions(driver).moveToElement(canvas).click().perform();
    }

    /**
     * Places at a point on the ground, not at a pixel on the screen.
     * <p>
     * The spiral in {@link #placeOnFreePosition()} says "somewhere that works", which is the right
     * thing when the position is incidental. Where the position <em>is</em> the quest - a dockyard
     * in the coastal region, a builder set down within a ship's reach - it is the wrong language:
     * a screen offset cannot express a ground condition, and when the placer refuses, the test
     * cannot say whether the spot was bad or the grid missed it.
     * <p>
     * Moves the camera there first, because a point off screen has no pixel to click.
     *
     * @return false when the placer refused the spot - the caller decides whether that is a
     * finding or just the next candidate
     */
    public boolean placeAt(double groundX, double groundY) {
        jsMoveCamera(groundX, groundY);
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {} // terrain and camera settle
        Object screen = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var point = svc.projectGroundPositionToScreen(" + groundX + ", " + groundY + ", 0);" +
                "if (!point) { return null; }" +
                "var canvas = svc.getScene().getEngine().getRenderingCanvas();" +
                "return [point.x - canvas.width / 2, point.y - canvas.height / 2];");
        if (!(screen instanceof List) || ((List<?>) screen).size() != 2) {
            System.out.println("[E2E] placeAt " + groundX + "/" + groundY + ": not on screen");
            return false;
        }
        int offsetX = (int) Math.round(((Number) ((List<?>) screen).get(0)).doubleValue());
        int offsetY = (int) Math.round(((Number) ((List<?>) screen).get(1)).doubleValue());
        clickCanvasAt(offsetX, offsetY);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2)).until(d -> isBaseItemPlacerInactive());
            System.out.println("[E2E] placed at " + groundX + "/" + groundY);
            return true;
        } catch (RuntimeException e) {
            System.out.println("[E2E] placer refused " + groundX + "/" + groundY
                    + " (canvas offset " + offsetX + "/" + offsetY + ")");
            return false;
        }
    }

    /** The terrain point under a canvas offset from the middle, as "x/y" - what a click there hits. */
    public String groundAt(int offsetX, int offsetY) {
        try {
            return String.valueOf(executeScript(
                    "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                    "var scene = svc.getScene();" +
                    "var canvas = scene.getEngine().getRenderingCanvas();" +
                    "var pick = scene.pick(canvas.width / 2 + " + offsetX + ", canvas.height / 2 + " + offsetY + ");" +
                    "if (!pick || !pick.pickedPoint) { return 'nothing picked'; }" +
                    "return Math.round(pick.pickedPoint.x) + '/' + Math.round(pick.pickedPoint.z);"));
        } catch (RuntimeException e) {
            return "not readable";
        }
    }

    public void clickCanvasAt(int offsetX, int offsetY) {
        WebElement canvas = driver.findElement(CANVAS);
        new Actions(driver).moveToElement(canvas, offsetX, offsetY).click().perform();
    }

    public void hoverCanvas() {
        WebElement canvas = driver.findElement(CANVAS);
        new Actions(driver).moveToElement(canvas).perform();
    }

    public void hoverCanvasAt(int offsetX, int offsetY) {
        WebElement canvas = driver.findElement(CANVAS);
        new Actions(driver).moveToElement(canvas, offsetX, offsetY).perform();
    }

    // ========== Cursor ==========

    public String getCursorStyle() {
        Object result = executeScript(
                "return document.querySelector('canvas.canvas').style.cursor;"
        );
        return result != null ? result.toString() : "";
    }

    public boolean isMoveCursor() {
        String cursor = getCursorStyle();
        return cursor.contains("go.png") || cursor.contains("go-no.png");
    }

    public boolean isNoGoCursor() {
        return getCursorStyle().contains("go-no.png");
    }

    public boolean isGoCursor() {
        String cursor = getCursorStyle();
        return cursor.contains("go.png") && !cursor.contains("go-no.png");
    }

    // ========== Terrain Position ==========

    public double[] getTerrainPosition() {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var pickInfo = svc.setupTerrainPickPoint();" +
                "if (pickInfo && pickInfo.pickedPoint) {" +
                "  return [pickInfo.pickedPoint.x, pickInfo.pickedPoint.z];" +
                "}" +
                "return null;"
        );
        if (result instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) result;
            return new double[]{((Number) list.get(0)).doubleValue(), ((Number) list.get(1)).doubleValue()};
        }
        return null;
    }

    public double[] hoverEmptyTerrain() {
        WebElement canvas = driver.findElement(CANVAS);
        java.util.List<int[]> offsets = new java.util.ArrayList<>();
        for (int x = -350; x <= 350; x += 50) {
            for (int y = -250; y <= 250; y += 50) {
                offsets.add(new int[]{x, y});
            }
        }
        for (int[] offset : offsets) {
            new Actions(driver).moveToElement(canvas, offset[0], offset[1]).perform();
            String cursor = getCursorStyle();
            if (cursor.contains("go.png") && !cursor.contains("go-no.png")) {
                return getTerrainPosition();
            }
        }
        throw new RuntimeException("Could not find empty terrain to hover after trying " + offsets.size() + " positions");
    }

    /**
     * Like hoverTerrainObject() but returns null instead of throwing if none found.
     */
    public double[] tryHoverTerrainObject() {
        try {
            return hoverTerrainObject();
        } catch (RuntimeException e) {
            return null;
        }
    }

    public double[] hoverTerrainObject() {
        WebElement canvas = driver.findElement(CANVAS);
        @SuppressWarnings("unchecked")
        java.util.List<Number> screenPos = (java.util.List<Number>) executeScript(
                "var scene = window.gwtAngularFacade.babylonRenderServiceAccess.getScene();" +
                "var engine = scene.getEngine();" +
                "var meshes = scene.meshes;" +
                "for (var i = 0; i < meshes.length; i++) {" +
                "  var mesh = meshes[i];" +
                "  if (!mesh.isPickable || !mesh.isVisible) continue;" +
                "  var node = mesh;" +
                "  while (node) {" +
                "    if (node.metadata && node.metadata.razarionMetadata && node.metadata.razarionMetadata.type === 1) {" +
                "      var center = mesh.getBoundingInfo().boundingBox.centerWorld;" +
                "      var Vector3 = center.constructor;" +
                "      var Matrix = scene.getTransformMatrix().constructor;" +
                "      var vp = scene.activeCamera.viewport.toGlobal(engine.getRenderWidth(), engine.getRenderHeight());" +
                "      var pos = Vector3.Project(center, Matrix.Identity(), scene.getTransformMatrix(), vp);" +
                "      if (pos.x >= 0 && pos.x <= engine.getRenderWidth() && pos.y >= 0 && pos.y <= engine.getRenderHeight()) {" +
                "        return [pos.x, pos.y];" +
                "      }" +
                "    }" +
                "    node = node.parent;" +
                "  }" +
                "}" +
                "return null;"
        );
        if (screenPos == null) {
            throw new RuntimeException("Could not find any terrain object in the scene");
        }
        int canvasWidth = canvas.getSize().getWidth();
        int canvasHeight = canvas.getSize().getHeight();
        int offsetX = screenPos.get(0).intValue() - canvasWidth / 2;
        int offsetY = screenPos.get(1).intValue() - canvasHeight / 2;
        new Actions(driver).moveToElement(canvas, offsetX, offsetY).perform();
        return getTerrainPosition();
    }

    // ========== Base Item Counts ==========

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

    public long getOwnItemCountByType(int itemTypeId) {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" + // 0 = OWN
                "var count = 0;" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getId() === " + itemTypeId + ") count++;" +
                "}" +
                "return count;"
        );
        return result instanceof Number ? ((Number) result).longValue() : 0;
    }

    public void waitForOwnItemCountByType(int itemTypeId, long expectedCount) {
        wait.until(d -> getOwnItemCountByType(itemTypeId) >= expectedCount);
    }

    // ========== Game Commands via JS ==========

    /**
     * Selects own items of the given type via JS (clicks not needed).
     */
    public void jsSelectOwnItemsByType(int itemTypeId) {
        executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var matching = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getId() === " + itemTypeId + ") matching.push(items[i]);" +
                "}" +
                "if (matching.length > 0) {" +
                "  svc.tsSelectionService.selectOwnItems(matching);" +
                "}"
        );
    }

    /**
     * Sends harvesters to the nearest resource item.
     */
    public void jsHarvestNearest() {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var harvesterIds = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getHarvesterType() != null) {" +
                "    harvesterIds.push(items[i].getId());" +
                "  }" +
                "}" +
                "if (harvesterIds.length === 0) return 'no harvesters found, own items: ' + items.length;" +
                "var resources = svc.getBabylonResourceItemImpls();" +
                "if (resources.length === 0) return 'no resources found';" +
                "gameCmd.harvestCmd(harvesterIds, resources[0].getId());" +
                "return 'harvest sent: harvesterIds=' + JSON.stringify(harvesterIds) + ' resourceId=' + resources[0].getId();"
        );
        System.out.println("[E2E] jsHarvestNearest: " + result);
    }

    /**
     * Sends attackers to the nearest enemy item.
     */
    public void jsAttackNearest() {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var attackerIds = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getWeaponType() != null) {" +
                "    attackerIds.push(items[i].getId());" +
                "  }" +
                "}" +
                "if (attackerIds.length === 0) return 'no attackers found, own items: ' + items.length;" +
                "var enemies = svc.getBabylonBaseItemsByDiplomacy('ENEMY');" +
                "if (enemies.length === 0) return 'no enemies found, attackers: ' + JSON.stringify(attackerIds);" +
                "var enemy = enemies[0];" +
                "var pos = enemy.getPosition();" +
                "gameCmd.attackCmd(attackerIds, enemy.getId());" +
                "return 'attack sent: attackerIds=' + JSON.stringify(attackerIds) + ' enemyId=' + enemy.getId() + ' enemyPos=' + (pos ? pos.getX()+','+pos.getY() : 'null') + ' totalEnemies=' + enemies.length;"
        );
        System.out.println("[E2E] jsAttackNearest: " + result);
    }

    /**
     * Moves the camera to the given terrain position.
     */
    /**
     * Moves the camera the way the minimap does, and says where it ended up.
     * <p>
     * It used to assign {@code scene.activeCamera.target.x/z}. The renderer uses a FreeCamera,
     * which has no such property to write, so the guard around it was false and the camera never
     * moved - while the line below printed that it had. Every step that clicked the canvas after
     * "moving" the camera was clicking wherever the camera still was; on Noob Island that is
     * harmless, and at the Phase 2 coast it put the unload four hundred units from the ship.
     * setViewFieldCenter is what the minimap calls, and it tells the engine about the new view.
     */
    public void jsMoveCamera(double x, double y) {
        executeScript("window.gwtAngularFacade.babylonRenderServiceAccess.setViewFieldCenter("
                + x + ", " + y + ");");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        System.out.println("[E2E] camera moved to " + x + "/" + y + ", ground under the centre: " + groundAt(0, 0));
    }

    /**
     * Attacks a specific enemy item type.
     * If the enemy is not rendered, moves the camera to the enemy position,
     * waits for it to be synced, then attacks.
     */
    public void jsAttackEnemyOfType(int enemyItemTypeId) {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "var baseUi = window.gwtAngularFacade.baseItemUiService;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var attackerIds = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getWeaponType() != null) {" +
                "    attackerIds.push(items[i].getId());" +
                "  }" +
                "}" +
                "if (attackerIds.length === 0) return 'no attackers found';" +
                // Try to find rendered enemy first
                "var enemies = svc.getBabylonBaseItemsByDiplomacy('ENEMY');" +
                "for (var i = 0; i < enemies.length; i++) {" +
                "  if (enemies[i].getBaseItemType().getId() === " + enemyItemTypeId + ") {" +
                "    gameCmd.attackCmd(attackerIds, enemies[i].getId());" +
                "    return 'attack rendered: enemyId=' + enemies[i].getId();" +
                "  }" +
                "}" +
                // Not rendered - try to get enemy ID from server-side service and attack by ID
                "var attacker = items[0];" +
                "var pos = attacker.getPosition();" +
                "if (!pos) return 'no attacker position';" +
                "try {" +
                "  var enemyId = baseUi.getNearestEnemyId(pos.getX(), pos.getY(), " + enemyItemTypeId + ");" +
                "  if (enemyId > 0) {" +
                "    gameCmd.attackCmd(attackerIds, enemyId);" +
                "    return 'attack by ID: enemyId=' + enemyId;" +
                "  }" +
                "} catch(e) {" +
                "  return 'getNearestEnemyId error: ' + e.message;" +
                "}" +
                // Fallback: get position for camera move
                "var enemyPos = baseUi.getNearestEnemyPosition(pos.getX(), pos.getY(), " + enemyItemTypeId + ", true);" +
                "if (!enemyPos) return 'enemy type " + enemyItemTypeId + " not found via baseUi';" +
                "return 'NOT_RENDERED:' + enemyPos.getX() + ':' + enemyPos.getY();"
        );
        String resultStr = result != null ? result.toString() : "";
        System.out.println("[E2E] jsAttackEnemyOfType(" + enemyItemTypeId + "): " + resultStr);

        if (resultStr.startsWith("NOT_RENDERED:")) {
            // Move camera to enemy position to trigger rendering
            String[] parts = resultStr.split(":");
            double enemyX = Double.parseDouble(parts[1]);
            double enemyY = Double.parseDouble(parts[2]);
            jsMoveCamera(enemyX, enemyY);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> jsHasEnemyOfType(enemyItemTypeId));
                System.out.println("[E2E] Enemy type " + enemyItemTypeId + " now rendered after camera move");
                jsAttackEnemyOfType(enemyItemTypeId);
            } catch (Exception e) {
                System.out.println("[E2E] Enemy type " + enemyItemTypeId + " still not rendered after camera move");
            }
        }
    }

    /**
     * Moves attackers toward the nearest enemy that is NOT of the excluded type.
     */
    public void jsMoveAttackersToEnemyExcludingType(int excludeItemTypeId) {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var attackerIds = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getWeaponType() != null) {" +
                "    attackerIds.push(items[i].getId());" +
                "  }" +
                "}" +
                "if (attackerIds.length === 0) return 'no attackers found';" +
                "var enemies = svc.getBabylonBaseItemsByDiplomacy('ENEMY');" +
                "for (var i = 0; i < enemies.length; i++) {" +
                "  if (enemies[i].getBaseItemType().getId() !== " + excludeItemTypeId + ") {" +
                "    var pos = enemies[i].getPosition();" +
                "    if (pos) {" +
                "      try {" +
                "        gameCmd.moveCmd(attackerIds, pos.getX(), pos.getY());" +
                "        return 'moving ' + JSON.stringify(attackerIds) + ' to enemy pos ' + pos.getX().toFixed(0) + ',' + pos.getY().toFixed(0);" +
                "      } catch(e) {" +
                "        return 'moveCmd ERROR: ' + e.message;" +
                "      }" +
                "    }" +
                "  }" +
                "}" +
                "return 'no non-excluded enemies found';"
        );
        System.out.println("[E2E] jsMoveAttackersToEnemyExcludingType(" + excludeItemTypeId + "): " + result);
    }

    /**
     * Sends attackers to the nearest enemy that is NOT of the excluded type.
     */
    public void jsAttackEnemyExcludingType(int excludeItemTypeId) {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var attackerIds = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getWeaponType() != null) {" +
                "    attackerIds.push(items[i].getId());" +
                "  }" +
                "}" +
                "if (attackerIds.length === 0) return 'no attackers found';" +
                "var enemies = svc.getBabylonBaseItemsByDiplomacy('ENEMY');" +
                "for (var i = 0; i < enemies.length; i++) {" +
                "  if (enemies[i].getBaseItemType().getId() !== " + excludeItemTypeId + ") {" +
                "    try {" +
                "      gameCmd.attackCmd(attackerIds, enemies[i].getId());" +
                "      return 'attack sent: attackerIds=' + JSON.stringify(attackerIds) + ' enemyId=' + enemies[i].getId() + ' type=' + enemies[i].getBaseItemType().getId();" +
                "    } catch(e) {" +
                "      return 'attackCmd ERROR: ' + e.message;" +
                "    }" +
                "  }" +
                "}" +
                "return 'no non-excluded enemies found';"
        );
        System.out.println("[E2E] jsAttackEnemyExcludingType(" + excludeItemTypeId + "): " + result);
    }

    /**
     * Repeatedly sends attack commands against enemies (excluding a type) until the quest advances.
     */
    public void jsAttackEnemyExcludingTypeUntilDone(int excludeItemTypeId) {
        // Wait for attacker to be fully built before commanding
        waitForAttackerReady();
        jsAttackEnemyExcludingType(excludeItemTypeId);
        waitForQuestDoneWithRetry(() -> {
            try { logAttackerState(); } catch (Exception e) { /* ignore logging errors */ }
            try { logBrowserErrors(); } catch (Exception e) { /* ignore logging errors */ }
            // If all attackers died, fabricate a new Viper
            if (getOwnItemCountByType(3) == 0) { // 3 = VIPER
                System.out.println("[E2E] No Vipers left, fabricating a new one");
                jsFabricate(4, 3); // Factory(4) -> Viper(3)
                waitForOwnItemCountByType(3, 1);
                waitForAttackerReady();
            }
            jsAttackEnemyExcludingType(excludeItemTypeId);
        }, "Destroy");
    }

    /**
     * Waits until at least one own attacker has buildup >= 1.0 and is not spawning.
     */
    private void waitForAttackerReady() {
        wait.until(d -> {
            Object result = executeScript(
                    "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                    "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                    "for (var i = 0; i < items.length; i++) {" +
                    "  if (items[i].getBaseItemType().getWeaponType() != null && items[i].getBuildup() >= 1.0) {" +
                    "    return true;" +
                    "  }" +
                    "}" +
                    "return false;"
            );
            return Boolean.TRUE.equals(result);
        });
        System.out.println("[E2E] attacker ready (buildup complete)");
    }

    /**
     * Repeatedly sends attack commands against a specific enemy type until the quest advances.
     */
    public void jsAttackEnemyOfTypeUntilDone(int enemyItemTypeId) {
        waitForAttackerReady();
        // Move vipers toward the enemy first (if not rendered, attackCmd won't work)
        jsMoveAttackersTowardEnemy(enemyItemTypeId);
        jsAttackEnemyOfType(enemyItemTypeId);
        waitForQuestDoneWithRetry(() -> {
            try { logViperPositions("retry"); } catch (Exception e) { /* ignore */ }
            try { logBrowserErrors(); } catch (Exception e) { /* ignore */ }
            // If attackers low, fabricate more Vipers
            long viperCount = getOwnItemCountByType(3); // 3 = VIPER
            if (viperCount < 2) {
                int toFabricate = (int)(4 - viperCount);
                System.out.println("[E2E] Only " + viperCount + " Vipers, fabricating " + toFabricate + " more");
                for (int i = 0; i < toFabricate; i++) {
                    jsFabricate(4, 3); // Factory(4) -> Viper(3)
                    waitForOwnItemCountByType(3, viperCount + i + 1);
                }
                waitForAttackerReady();
            }
            jsMoveAttackersTowardEnemy(enemyItemTypeId);
            jsAttackEnemyOfType(enemyItemTypeId);
        }, "Destroy");
    }

    /**
     * Moves attackers toward the nearest enemy of the given type using moveCmd.
     */
    public void jsMoveAttackersTowardEnemy(int enemyItemTypeId) {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "var baseUi = window.gwtAngularFacade.baseItemUiService;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var attackerIds = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getWeaponType() != null) {" +
                "    attackerIds.push(items[i].getId());" +
                "  }" +
                "}" +
                "if (attackerIds.length === 0) return 'no attackers';" +
                // Check if enemy is already rendered
                "var enemies = svc.getBabylonBaseItemsByDiplomacy('ENEMY');" +
                "for (var i = 0; i < enemies.length; i++) {" +
                "  if (enemies[i].getBaseItemType().getId() === " + enemyItemTypeId + ") {" +
                "    return 'enemy already rendered';" +
                "  }" +
                "}" +
                // Enemy not rendered - get position and move toward it
                "var pos = items[0].getPosition();" +
                "if (!pos) return 'no position';" +
                "var enemyPos = baseUi.getNearestEnemyPosition(pos.getX(), pos.getY(), " + enemyItemTypeId + ", true);" +
                "if (!enemyPos) return 'enemy not found';" +
                "gameCmd.moveCmd(attackerIds, enemyPos.getX(), enemyPos.getY());" +
                "return 'moving ' + attackerIds.length + ' attackers toward ' + enemyPos.getX().toFixed(0) + ',' + enemyPos.getY().toFixed(0);"
        );
        System.out.println("[E2E] jsMoveAttackersTowardEnemy(" + enemyItemTypeId + "): " + result);
    }

    /**
     * Logs positions of all own vipers (type 3).
     */
    public void logViperPositions(String label) {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var info = '';" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getId() === 3) {" +
                "    var pos = items[i].getPosition();" +
                "    info += 'viper#' + items[i].getId() + '@' + (pos ? pos.getX().toFixed(1) + ',' + pos.getY().toFixed(1) : 'null') + ' ';" +
                "  }" +
                "}" +
                "return info || 'NO VIPERS';"
        );
        System.out.println("[E2E] viperPositions[" + label + "]: " + result);
    }

    public void logBrowserErrors() {
        Object result = executeScript(
                "if (!window.__e2eErrors) return 'no error capture';" +
                "var errors = window.__e2eErrors.splice(0);" +
                "return errors.length > 0 ? errors.join(' | ') : 'no errors';"
        );
        System.out.println("[E2E] browser errors: " + result);
    }

    public void logGameMode() {
        Object result = executeScript(
                "try {" +
                "  var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "  var scene = svc.getScene();" +
                "  return 'scene meshes: ' + scene.meshes.length;" +
                "} catch(e) { return 'error: ' + e.message; }"
        );
        System.out.println("[E2E] gameMode info: " + result);
    }

    public void setupErrorCapture() {
        executeScript(
                "window.__e2eErrors = window.__e2eErrors || [];" +
                "if (!window.__e2eErrorCaptureSet) {" +
                "  window.__e2eErrorCaptureSet = true;" +
                "  var origError = console.error;" +
                "  console.error = function() {" +
                "    var msg = Array.from(arguments).join(' ');" +
                "    window.__e2eErrors.push(msg);" +
                "    origError.apply(console, arguments);" +
                "  };" +
                "  var origWarn = console.warn;" +
                "  console.warn = function() {" +
                "    var msg = Array.from(arguments).join(' ');" +
                "    if (msg.indexOf('WASM') !== -1 || msg.indexOf('worker') !== -1 || msg.indexOf('error') !== -1) {" +
                "      window.__e2eErrors.push('WARN: ' + msg);" +
                "    }" +
                "    origWarn.apply(console, arguments);" +
                "  };" +
                "  window.addEventListener('error', function(e) {" +
                "    window.__e2eErrors.push('UNCAUGHT: ' + e.message);" +
                "  });" +
                "}"
        );
    }

    public void logAttackerState() {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var enemies = svc.getBabylonBaseItemsByDiplomacy('ENEMY');" +
                "var info = 'own:['; " +
                "for (var i = 0; i < items.length; i++) {" +
                "  var it = items[i]; var pos = it.getPosition();" +
                "  info += '{id:'+it.getId()+',type:'+it.getBaseItemType().getId()+" +
                "    ',pos:'+(pos?pos.getX().toFixed(0)+','+pos.getY().toFixed(0):'null')+" +
                "    ',weapon:'+(it.getBaseItemType().getWeaponType()!=null)+" +
                "    ',buildup:'+it.getBuildup().toFixed(2)+'}';" +
                "  if(i<items.length-1) info+=',';" +
                "}" +
                "info += '] enemies:['; " +
                "for (var i = 0; i < enemies.length; i++) {" +
                "  var en = enemies[i]; var epos = en.getPosition();" +
                "  info += '{id:'+en.getId()+',type:'+en.getBaseItemType().getId()+" +
                "    ',pos:'+(epos?epos.getX().toFixed(0)+','+epos.getY().toFixed(0):'null')+'}';" +
                "  if(i<enemies.length-1) info+=',';" +
                "}" +
                "info += ']';" +
                "return info;"
        );
        System.out.println("[E2E] state: " + result);
    }

    /**
     * Public version: polls quest completion, re-executing the action every 10s.
     */
    public void waitForQuestCompletedWithRetry(Runnable retryAction, String currentQuestTitle, int timeoutSeconds) {
        waitForQuestDoneWithRetry(retryAction, currentQuestTitle, timeoutSeconds);
    }

    /**
     * Polls quest completion, re-executing the action every 10s in case units are idle.
     */
    private void waitForQuestDoneWithRetry(Runnable retryAction, String currentQuestTitle) {
        waitForQuestDoneWithRetry(retryAction, currentQuestTitle, 120);
    }

    private void waitForQuestDoneWithRetry(Runnable retryAction, String currentQuestTitle, int timeoutSeconds) {
        final long[] lastRetry = {System.currentTimeMillis()};
        final long[] lastLog = {0};
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
            long now = System.currentTimeMillis();

            // Log every 5 seconds
            if (now - lastLog[0] > 5000) {
                try {
                    String title = getQuestTitle();
                    System.out.println("[E2E] waitForQuestDone('" + currentQuestTitle + "') title='" + title + "'");
                    if (!currentQuestTitle.equals(title)) return true;
                } catch (Exception e) {
                    System.out.println("[E2E] waitForQuestDone: getQuestTitle error: " + e.getClass().getSimpleName());
                }
                lastLog[0] = now;
            }

            // Retry action every 10s
            if (now - lastRetry[0] > 10000) {
                try {
                    retryAction.run();
                } catch (Exception e) {
                    System.out.println("[E2E] waitForQuestDone: retry error: " + e.getMessage());
                }
                lastRetry[0] = now;
            }

            // Quick check without logging
            try {
                String title = getQuestTitle();
                if (!currentQuestTitle.equals(title)) return true;
                return isQuestProgressAllDone();
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Moves own items of the given type to the specified terrain position.
     */
    public void jsMoveItemsOfType(int itemTypeId, double x, double y) {
        List<Long> ids = jsOwnItemIdsOfType(itemTypeId);
        if (ids.isEmpty()) {
            System.out.println("[E2E] jsMoveItemsOfType: no item of type " + itemTypeId + " on the planet");
            return;
        }
        executeScript("window.gwtAngularFacade.gameCommandService.moveCmd(" + ids + ", " + x + ", " + y + ");");
    }

    /**
     * Fabricates a unit from a factory via JS command.
     */
    /**
     * Own items of a type over the whole planet, as {@code [{id, x, y, idle, buildup}]}.
     * <p>
     * Everything else here reads the renderer, which only knows what is on screen - the dockyard on
     * the coast, a viper that drove off, the transporter halfway to the second island are all
     * invisible to it, and a step that looks for them finds nothing and waits out its timeout. The
     * worker knows them all; {@code getTipItemStates} is the query that asks it.
     */
    @SuppressWarnings("unchecked")
    public List<java.util.Map<String, Object>> jsOwnItemsOfType(int itemTypeId) {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.baseItemUiService;" +
                "if (!svc || !svc.getTipItemStates) { return []; }" +
                "return svc.getTipItemStates(-1).filter(function (item) {" +
                "  return item.own && item.itemTypeId === " + itemTypeId + ";" +
                "}).map(function (item) {" +
                "  return {id: item.id, x: item.x, y: item.y, idle: item.idle, buildup: item.buildup};" +
                "});");
        return result instanceof List ? (List<java.util.Map<String, Object>>) result : new ArrayList<>();
    }

    /**
     * Enemies of a type over the whole planet, the same way {@link #jsOwnItemsOfType} finds own
     * ones: the query takes an enemy type and returns those too, which is how the quest tip points
     * at a target that was never on screen.
     */
    @SuppressWarnings("unchecked")
    public List<java.util.Map<String, Object>> jsEnemyItemsOfType(int itemTypeId) {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.baseItemUiService;" +
                "if (!svc || !svc.getTipItemStates) { return []; }" +
                "return svc.getTipItemStates(" + itemTypeId + ").filter(function (item) {" +
                "  return !item.own && item.itemTypeId === " + itemTypeId + ";" +
                "}).map(function (item) {" +
                "  return {id: item.id, x: item.x, y: item.y};" +
                "});");
        return result instanceof List ? (List<java.util.Map<String, Object>>) result : new ArrayList<>();
    }

    /**
     * Sends everything of one type at the nearest enemy of another, wherever both are. The older
     * attack helper is about vipers on land and rebuilds them from the factory; this one is the
     * plain order, which is what the water fight of quest 388 needs.
     */
    public void jsAttackWithType(int attackerTypeId, int enemyTypeId) {
        List<Long> attackerIds = jsOwnItemIdsOfType(attackerTypeId);
        List<java.util.Map<String, Object>> enemies = jsEnemyItemsOfType(enemyTypeId);
        if (attackerIds.isEmpty() || enemies.isEmpty()) {
            System.out.println("[E2E] jsAttackWithType: attackers=" + attackerIds.size()
                    + " enemies of type " + enemyTypeId + "=" + enemies.size());
            return;
        }
        // The nearest one, not the first the engine happens to list: the bot keeps two dozen of
        // them spread over the water, and sending a ship across the map to the far one loses it.
        java.util.Map<String, Object> first = jsOwnItemsOfType(attackerTypeId).get(0);
        double fromX = ((Number) first.get("x")).doubleValue();
        double fromY = ((Number) first.get("y")).doubleValue();
        java.util.Map<String, Object> enemy = enemies.stream()
                .min(java.util.Comparator.comparingDouble(candidate ->
                        Math.hypot(((Number) candidate.get("x")).doubleValue() - fromX,
                                ((Number) candidate.get("y")).doubleValue() - fromY)))
                .orElseThrow();
        long targetId = ((Number) enemy.get("id")).longValue();
        double enemyX = ((Number) enemy.get("x")).doubleValue();
        double enemyY = ((Number) enemy.get("y")).doubleValue();
        java.util.Map<String, Object> attacker = jsOwnItemsOfType(attackerTypeId).get(0);
        double attackerX = ((Number) attacker.get("x")).doubleValue();
        double attackerY = ((Number) attacker.get("y")).doubleValue();
        double distance = Math.hypot(attackerX - enemyX, attackerY - enemyY);
        System.out.printf("[E2E] attack %d at %.0f/%.0f with %s at %.0f/%.0f, distance %.0f, idle=%s%n",
                targetId, enemyX, enemyY, attackerIds, attackerX, attackerY, distance, attacker.get("idle"));
        executeScript("window.gwtAngularFacade.gameCommandService.attackCmd(" + attackerIds + ", " + targetId + ");");
    }

    /** The ids of all own items of a type, wherever they are. */
    public List<Long> jsOwnItemIdsOfType(int itemTypeId) {
        List<Long> ids = new ArrayList<>();
        for (java.util.Map<String, Object> item : jsOwnItemsOfType(itemTypeId)) {
            ids.add(((Number) item.get("id")).longValue());
        }
        return ids;
    }

    public void jsFabricate(int factoryItemTypeId, int unitItemTypeId) {
        List<Long> factoryIds = jsOwnItemIdsOfType(factoryItemTypeId);
        if (factoryIds.isEmpty()) {
            System.out.println("[E2E] jsFabricate: no item of type " + factoryItemTypeId + " on the planet");
            return;
        }
        executeScript(
                "var bridge = window.gwtAngularFacade.itemCockpitBridge;" +
                "if (bridge) { bridge.requestFabricate(" + factoryIds + ", " + unitItemTypeId + "); }"
        );
    }

    /**
     * Sells all own items of the given type.
     */
    public void jsSellItemsOfType(int itemTypeId) {
        executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var bridge = window.gwtAngularFacade.itemCockpitBridge;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var ids = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getId() === " + itemTypeId + ") {" +
                "    ids.push(items[i].getId());" +
                "  }" +
                "}" +
                "if (ids.length > 0 && bridge) {" +
                "  bridge.sellItems(ids);" +
                "}"
        );
    }

    /**
     * Loads items into a transporter.
     */
    public void jsLoadIntoTransporter(int itemTypeIdToLoad) {
        List<Long> transporterIds = jsOwnItemIdsOfType(TRANSPORTER_TYPE_ID);
        List<Long> loadIds = jsOwnItemIdsOfType(itemTypeIdToLoad);
        if (transporterIds.isEmpty() || loadIds.isEmpty()) {
            System.out.println("[E2E] jsLoadIntoTransporter: transporters=" + transporterIds.size()
                    + " to load=" + loadIds.size());
            return;
        }
        System.out.println("[E2E] load " + loadIds + " into transporter " + transporterIds.get(0));
        executeScript("window.gwtAngularFacade.gameCommandService.loadContainerCmd("
                + loadIds + ", " + transporterIds.get(0) + ");");
    }

    /** Whether the transporter is carrying anything - the load half of quest 392, from outside. */
    public boolean isTransporterLoaded(int itemTypeIdLoaded) {
        return jsOwnItemsOfType(itemTypeIdLoaded).isEmpty() && !jsOwnItemsOfType(TRANSPORTER_TYPE_ID).isEmpty();
    }

    /**
     * How far from a container a unit may be set down. The placer does not know this number - it
     * checks terrain and neighbours - so a spot it accepts can still be one the engine refuses.
     */
    public Object jsContainerRange(int containerItemTypeId) {
        return executeScript(
                "try {" +
                "  var type = window.gwtAngularFacade.itemTypeService.getBaseItemTypeAngular(" + containerItemTypeId + ");" +
                "  var container = type.getItemContainerType();" +
                "  return container ? container.getRange() : 'no container type';" +
                "} catch (e) { return 'not readable: ' + e; }");
    }

    /** Waits for a condition of the game itself, and says what it was waiting for when it fails. */
    public void waitUntil(java.util.function.BooleanSupplier condition, int timeoutSeconds, String what) {
        System.out.println("[E2E] waiting for " + what);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
                try {
                    return condition.getAsBoolean();
                } catch (RuntimeException e) {
                    return false;
                }
            });
        } catch (RuntimeException e) {
            throw new RuntimeException("waited in vain for " + what, e);
        }
    }

    /** Whether a position is within a radius of a point; null (nothing there) is never near. */
    public boolean isNear(double[] position, double x, double y, double radius) {
        return position != null && Math.hypot(position[0] - x, position[1] - y) <= radius;
    }

    /** Where an own item of this type stands, or null if there is none. */
    public double[] jsPositionOfType(int itemTypeId) {
        List<java.util.Map<String, Object>> items = jsOwnItemsOfType(itemTypeId);
        if (items.isEmpty()) {
            return null;
        }
        return new double[]{((Number) items.get(0).get("x")).doubleValue(),
                ((Number) items.get(0).get("y")).doubleValue()};
    }

    /**
     * Unloads a transporter.
     */
    /**
     * Presses Unload on the transporter. That opens the base item placer - the player then picks
     * the spot the unit steps out on - so a placement has to follow, exactly as for a building.
     */
    public void jsUnloadTransporter() {
        List<Long> transporterIds = jsOwnItemIdsOfType(TRANSPORTER_TYPE_ID);
        if (transporterIds.isEmpty()) {
            System.out.println("[E2E] jsUnloadTransporter: no transporter on the planet");
            return;
        }
        executeScript("var bridge = window.gwtAngularFacade.itemCockpitBridge;"
                + "if (bridge) { bridge.requestUnload(" + transporterIds.get(0) + "); }");
    }

    /**
     * Gets the game ID of the first own item of given type (requires item to be rendered).
     */
    /**
     * The id of an own item of this type, wherever it is on the planet, or -1.
     * <p>
     * Asks the worker, not the renderer. The renderer knows what is on screen, so this used to
     * answer -1 for a builder that had walked off, and the caller then sent its orders to id -1
     * and waited out a two-minute timeout on a quest that was never given a chance.
     */
    public int jsGetOwnItemId(int itemTypeId) {
        List<Long> ids = jsOwnItemIdsOfType(itemTypeId);
        return ids.isEmpty() ? -1 : ids.get(0).intValue();
    }

    /**
     * Sends moveCmd for a specific item ID to terrain coordinates (works regardless of rendering).
     */
    public void jsMoveById(int itemId, double x, double y) {
        Object result = executeScript(
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "gameCmd.moveCmd([" + itemId + "], " + x + ", " + y + ");" +
                "return 'moveCmd sent: id=" + itemId + " to " + x + "," + y + "';"
        );
        System.out.println("[E2E] " + result);
    }

    /**
     * Sends buildCmd for a specific builder ID (works regardless of rendering).
     */
    public void jsBuildById(int builderId, int itemTypeId, double x, double y) {
        Object result = executeScript(
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "try {" +
                "  gameCmd.buildCmd(" + builderId + ", " + x + ", " + y + ", " + itemTypeId + ");" +
                "  return 'buildCmd sent: builder=" + builderId + " at " + x + "," + y + " type=" + itemTypeId + "';" +
                "} catch(e) {" +
                "  return 'buildCmd error: ' + e.message;" +
                "}"
        );
        System.out.println("[E2E] " + result);
    }

    /**
     * Gets position of first own item of given type as [x, y].
     */
    public double[] jsGetOwnItemPosition(int itemTypeId) {
        Object result = executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getId() === " + itemTypeId + ") {" +
                "    var pos = items[i].getPosition();" +
                "    if (pos) return [pos.getX(), pos.getY()];" +
                "  }" +
                "}" +
                "return null;"
        );
        if (result instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) result;
            return new double[]{((Number) list.get(0)).doubleValue(), ((Number) list.get(1)).doubleValue()};
        }
        return null;
    }

    /**
     * Checks if there are enemy items of the given type visible.
     */
    public boolean jsHasEnemyOfType(int enemyItemTypeId) {
        return Boolean.TRUE.equals(executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var enemies = svc.getBabylonBaseItemsByDiplomacy('ENEMY');" +
                "for (var i = 0; i < enemies.length; i++) {" +
                "  if (enemies[i].getBaseItemType().getId() === " + enemyItemTypeId + ") return true;" +
                "}" +
                "return false;"
        ));
    }

    // ========== Cockpit Verification Helpers ==========

    /**
     * Verifies main cockpit is visible and shows the expected level.
     */
    public void verifyMainCockpit(int expectedLevel) {
        assertThatVisible(MAIN_COCKPIT, "Main cockpit");
        waitForLevel(expectedLevel);
    }

    /**
     * Verifies quest cockpit shows expected title.
     */
    public void verifyQuestCockpit(String expectedTitle) {
        waitForQuestCockpitVisible();
        waitForQuestTitle(expectedTitle);
    }

    /**
     * Verifies cursor behavior: hovers empty terrain (go) and a terrain object (go-no).
     * Finding a terrain object stays best-effort (they stream in asynchronously), but once one is
     * hovered the no-go cursor is asserted - that is the regression this test exists for.
     */
    public void verifyCursors() {
        double[] emptyPos = hoverEmptyTerrain();
        if (emptyPos == null) {
            throw new AssertionError("No terrain position under the pointer after hovering empty terrain");
        }
        double[] objPos = tryHoverTerrainObject();
        if (objPos == null) {
            // No terrain object visible/loaded yet - nothing to assert.
            return;
        }
        // The cursor is throttled, so give it one window to catch up.
        wait.until(d -> isNoGoCursor());
    }

    /**
     * Selects builder by clicking canvas center, waits for item cockpit.
     */
    public void selectBuilderViaClick() {
        clickCanvas();
        waitForItemCockpitVisible();
    }

    /**
     * Selects an own item by projecting its 3D position to screen coords and clicking there.
     */
    public void selectItemByType(int itemTypeId) {
        String selectScript =
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getId() === " + itemTypeId + ") {" +
                "    var item = items[i];" +
                "    if (window.__e2eNgZone) {" +
                "      window.__e2eNgZone.run(function() {" +
                "        svc.actionService.onItemClicked(item.itemType, item.getId(), 'OWN', item);" +
                "      });" +
                "    } else {" +
                "      svc.actionService.onItemClicked(item.itemType, item.getId(), 'OWN', item);" +
                "    }" +
                "    return true;" +
                "  }" +
                "}" +
                "return false;";
        // Retry selection with Angular ticks until item cockpit becomes visible
        wait.until(d -> {
            executeScript(selectScript);
            executeScript("if (window.__e2eAppRef) { window.__e2eAppRef.tick(); }");
            return !d.findElements(By.cssSelector("item-cockpit")).isEmpty()
                    && d.findElement(By.cssSelector("item-cockpit")).isDisplayed();
        });
    }

    /**
     * Builds an item using the builder: clicks build button, waits for placer, places it.
     */
    public void buildViaBuilder(int itemTypeId) {
        // Own items of this type, not the scene's node count. That count includes every bot unit
        // in the scene, so it rises and falls with traffic that has nothing to do with the
        // building - and a bot walking off screen while the site goes up hides the very thing
        // the last step waits for.
        int ownBefore = jsOwnItemsOfType(itemTypeId).size();
        // Named steps, because the four waits in here all fail the same way from the outside - a
        // timeout on a lambda - and the stack trace then says only "buildViaBuilder".
        step("build " + itemTypeId + ": wait for the button", () -> waitForBuildButtonForItemType(itemTypeId));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {} // Let carousel settle
        step("build " + itemTypeId + ": click the button", () -> clickBuildButtonForItemType(itemTypeId));
        step("build " + itemTypeId + ": wait for the placer", this::waitForBaseItemPlacerActive);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {} // Let placer initialize
        step("build " + itemTypeId + ": place it", this::placeOnFreePosition);
        step("build " + itemTypeId + ": wait for the item",
                () -> waitUntil(() -> jsOwnItemsOfType(itemTypeId).size() > ownBefore, 60,
                        "a new item of type " + itemTypeId));
    }

    /** Runs a step, says so, and names it again if it throws. */
    private void step(String what, Runnable body) {
        System.out.println("[E2E] " + what);
        try {
            body.run();
        } catch (RuntimeException e) {
            System.out.println("[E2E] FAILED: " + what + " -> " + e.getClass().getSimpleName());
            throw new RuntimeException(what, e);
        }
    }

    /**
     * Builds with quest region awareness.
     * If quest region center is available via bridge, uses buildCmd directly.
     * Otherwise, activates placer and searches camera positions.
     */
    public void buildViaBuilderInQuestRegion(int itemTypeId) {
        long countBefore = getBaseItemCount();

        // Try direct buildCmd with quest region coordinates
        double[] regionCenter = getQuestRegionCenter();
        if (regionCenter != null) {
            System.out.println("[E2E] Building " + itemTypeId + " via buildCmd at quest region: " + regionCenter[0] + ", " + regionCenter[1]);
            jsBuildAtPosition(itemTypeId, regionCenter[0], regionCenter[1]);
            waitForBaseItemCountAbove(countBefore);
            return;
        }

        // Fallback: use placer UI + camera grid search
        System.out.println("[E2E] No quest region from bridge, using placer UI fallback");
        waitForBuildButtonForItemType(itemTypeId);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        clickBuildButtonForItemType(itemTypeId);
        waitForBaseItemPlacerActive();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        placeInQuestRegion();
        waitForBaseItemCountAbove(countBefore);
    }

    private void assertThatVisible(By selector, String name) {
        if (!driver.findElement(selector).isDisplayed()) {
            throw new AssertionError(name + " is not visible");
        }
    }

    private Object executeScript(String script) {
        return ((JavascriptExecutor) driver).executeScript(script);
    }
}
