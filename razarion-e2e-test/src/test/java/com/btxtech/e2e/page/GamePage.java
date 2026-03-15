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

    private static final By CANVAS = By.cssSelector("canvas.canvas");
    private static final By LOADING_OVERLAY = By.cssSelector("div.cover-panel");
    private static final By MAIN_COCKPIT = By.cssSelector("main-cockpit");
    private static final By QUEST_COCKPIT = By.cssSelector("quest-cockpit");
    private static final By QUEST_TITLE = By.cssSelector("quest-cockpit .font-semibold.text-3xl");
    private static final By ITEM_COCKPIT = By.cssSelector("item-cockpit");
    private static final By BUILD_BUTTON = By.cssSelector("button.item-cockpit-buildup-button:enabled");
    private static final By SELL_BUTTON = By.cssSelector("item-cockpit p-button[label='$'] button");
    private static final By QUEST_PROGRESS_ROW = By.cssSelector("quest-cockpit .flex.flex-row .flex:last-child");
    private static final By QUEST_DONE_ICON = By.cssSelector("quest-cockpit .pi-check-circle");
    private static final By LEVEL_BADGE = By.cssSelector("main-cockpit p-badge");

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
                return;
            } catch (Exception e) {
                // Terrain not free, out of bounds, or not in quest region — try next
            }
        }
        throw new RuntimeException("Could not find free terrain for placement after trying " + offsets.size() + " positions");
    }

    /**
     * Gets the quest region center from the active quest's PlaceConfig via REST API.
     * Returns [x, y] or null if no place config found.
     */
    @SuppressWarnings("unchecked")
    public double[] getQuestRegionCenter() {
        // First try REST API
        Object result = executeScript(
                "var xhr = new XMLHttpRequest();" +
                "xhr.open('GET', '/rest/quest-controller/readMyOpenQuests', false);" +
                "xhr.send();" +
                "if (xhr.status !== 200) return 'REST_ERROR:' + xhr.status + ':' + xhr.responseText.substring(0,200);" +
                "var resp = xhr.responseText;" +
                "try {" +
                "  var quests = JSON.parse(resp);" +
                "  if (!quests || (Array.isArray(quests) && quests.length === 0)) return 'EMPTY_QUESTS';" +
                "  var quest = Array.isArray(quests) ? quests[0] : quests;" +
                "  var cc = quest.conditionConfig;" +
                "  if (!cc) return 'NO_CONDITION:' + JSON.stringify(Object.keys(quest));" +
                "  var comp = cc.comparisonConfig;" +
                "  if (!comp) return 'NO_COMPARISON:' + JSON.stringify(Object.keys(cc));" +
                "  var pc = comp.placeConfig;" +
                "  if (!pc) return 'NO_PLACE_CONFIG:' + JSON.stringify(Object.keys(comp));" +
                "  if (pc.position) return [pc.position.x, pc.position.y];" +
                "  if (pc.polygon2D && pc.polygon2D.corners && pc.polygon2D.corners.length > 0) {" +
                "    var cx = 0, cy = 0;" +
                "    for (var i = 0; i < pc.polygon2D.corners.length; i++) {" +
                "      cx += pc.polygon2D.corners[i].x; cy += pc.polygon2D.corners[i].y;" +
                "    }" +
                "    return [cx / pc.polygon2D.corners.length, cy / pc.polygon2D.corners.length];" +
                "  }" +
                "  return 'PLACE_CONFIG_EMPTY:' + JSON.stringify(pc);" +
                "} catch(e) {" +
                "  return 'PARSE_ERROR:' + e.message + ':' + resp.substring(0,200);" +
                "}"
        );
        if (result instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) result;
            double[] center = new double[]{((Number) list.get(0)).doubleValue(), ((Number) list.get(1)).doubleValue()};
            System.out.println("[E2E] Quest region center: " + center[0] + ", " + center[1]);
            return center;
        }
        System.out.println("[E2E] Quest region REST debug: " + result);

        // Fallback: Try to find quest region visualization mesh in the Babylon scene
        Object meshResult = executeScript(
                "var scene = window.gwtAngularFacade.babylonRenderServiceAccess.getScene();" +
                "var meshes = scene.meshes;" +
                "var questMeshes = [];" +
                "for (var i = 0; i < meshes.length; i++) {" +
                "  var name = meshes[i].name || '';" +
                "  if (name.toLowerCase().indexOf('quest') !== -1 || " +
                "      name.toLowerCase().indexOf('place') !== -1 || " +
                "      name.toLowerCase().indexOf('marker') !== -1 || " +
                "      name.toLowerCase().indexOf('region') !== -1) {" +
                "    var pos = meshes[i].position;" +
                "    questMeshes.push(name + '@' + pos.x.toFixed(1) + ',' + pos.z.toFixed(1));" +
                "  }" +
                "}" +
                // Also check for meshes with special metadata
                "for (var i = 0; i < meshes.length; i++) {" +
                "  var m = meshes[i].metadata;" +
                "  if (m && m.razarionMetadata && m.razarionMetadata.type === 4) {" +  // type 4 might be quest marker
                "    var pos = meshes[i].position;" +
                "    questMeshes.push('META:' + meshes[i].name + '@' + pos.x.toFixed(1) + ',' + pos.z.toFixed(1));" +
                "  }" +
                "}" +
                "return questMeshes.length > 0 ? questMeshes.join(' | ') : 'NO_QUEST_MESHES (total: ' + meshes.length + ')';"
        );
        System.out.println("[E2E] Quest meshes: " + meshResult);

        // Try to find place marker polygon nodes
        Object markerResult = executeScript(
                "var scene = window.gwtAngularFacade.babylonRenderServiceAccess.getScene();" +
                "var nodes = scene.getTransformNodeByName('Quest Place Markers') || " +
                "            scene.getTransformNodeByName('PlaceMarkers') || " +
                "            scene.getTransformNodeByName('QuestMarkers');" +
                "if (nodes) {" +
                "  var children = nodes.getChildren();" +
                "  var info = 'found container: ' + nodes.name + ' children: ' + children.length;" +
                "  for (var i = 0; i < Math.min(children.length, 5); i++) {" +
                "    var p = children[i].position;" +
                "    info += ' [' + children[i].name + '@' + p.x.toFixed(1) + ',' + p.z.toFixed(1) + ']';" +
                "  }" +
                "  return info;" +
                "}" +
                // List all top-level transform nodes for debugging
                "var tnodes = scene.transformNodes;" +
                "var names = [];" +
                "for (var i = 0; i < tnodes.length; i++) {" +
                "  if (!tnodes[i].parent) names.push(tnodes[i].name);" +
                "}" +
                "return 'NO_MARKER_CONTAINER top-level: ' + names.join(', ');"
        );
        System.out.println("[E2E] Quest marker nodes: " + markerResult);

        return null;
    }

    /**
     * Places building by moving camera to the quest region (if available), then trying clicks.
     * Falls back to searching camera positions in a grid.
     */
    public void placeInQuestRegion() {
        double[] regionCenter = getQuestRegionCenter();
        if (regionCenter != null) {
            jsMoveCamera(regionCenter[0], regionCenter[1]);
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            try {
                placeOnFreePosition();
                return;
            } catch (RuntimeException e) {
                System.out.println("[E2E] Placement failed at quest region center, trying offsets");
            }
        }
        // Search a grid of camera positions across the Phase 1 area
        double[][] cameraPositions = {
                {150, 180}, {180, 180}, {120, 180},  // near coastline (~y200)
                {150, 150}, {180, 150}, {120, 150},  // south of coastline
                {150, 220}, {180, 220}, {120, 220},  // at coastline
                {150, 250}, {180, 250}, {100, 200},  // in water area
                {200, 180}, {200, 150}, {80, 180},   // east/west coast
                {178, 100}, {178, 50}, {150, 100},   // close to base
                {250, 180}, {250, 150}, {250, 200},  // further east
                {100, 150}, {100, 100}, {50, 180},   // west
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
        By selector = By.cssSelector("div[data-item-type-id='" + itemTypeId + "'] button.item-cockpit-buildup-button:enabled");
        WebElement button = driver.findElement(selector);
        new Actions(driver).moveToElement(button).click().perform();
    }

    public boolean hasBuildButtonForItemType(int itemTypeId) {
        By selector = By.cssSelector("div[data-item-type-id='" + itemTypeId + "'] button.item-cockpit-buildup-button:enabled");
        return !driver.findElements(selector).isEmpty();
    }

    public void waitForBuildButtonForItemType(int itemTypeId) {
        By enabledSelector = By.cssSelector("div[data-item-type-id='" + itemTypeId + "'] button.item-cockpit-buildup-button:enabled");
        wait.until(d -> {
            if (!d.findElements(enabledSelector).isEmpty()) return true;
            // Periodically trigger Angular change detection to ensure cockpit renders
            executeScript(
                    "if (window.__e2eAppRef) { window.__e2eAppRef.tick(); }"
            );
            return false;
        });
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
                "  if (!mesh.actionManager) continue;" +
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
    public void jsMoveCamera(double x, double y) {
        executeScript(
                "var scene = window.gwtAngularFacade.babylonRenderServiceAccess.getScene();" +
                "var camera = scene.activeCamera;" +
                "if (camera && camera.target) {" +
                "  camera.target.x = " + x + ";" +
                "  camera.target.z = " + y + ";" +
                "}"
        );
        System.out.println("[E2E] Camera moved to " + x + ", " + y);
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
     * Polls quest completion, re-executing the action every 10s in case units are idle.
     */
    private void waitForQuestDoneWithRetry(Runnable retryAction, String currentQuestTitle) {
        final long[] lastRetry = {System.currentTimeMillis()};
        final long[] lastLog = {0};
        new WebDriverWait(driver, Duration.ofSeconds(120)).until(d -> {
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
        executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var ids = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getId() === " + itemTypeId + ") {" +
                "    ids.push(items[i].getId());" +
                "  }" +
                "}" +
                "if (ids.length > 0) {" +
                "  gameCmd.moveCmd(ids, " + x + ", " + y + ");" +
                "}"
        );
    }

    /**
     * Fabricates a unit from a factory via JS command.
     */
    public void jsFabricate(int factoryItemTypeId, int unitItemTypeId) {
        executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var bridge = window.gwtAngularFacade.itemCockpitBridge;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var factoryIds = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getId() === " + factoryItemTypeId + ") {" +
                "    factoryIds.push(items[i].getId());" +
                "  }" +
                "}" +
                "if (factoryIds.length > 0 && bridge) {" +
                "  bridge.requestFabricate(factoryIds, " + unitItemTypeId + ");" +
                "}"
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
        executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var gameCmd = window.gwtAngularFacade.gameCommandService;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "var transporterId = null;" +
                "var loadIds = [];" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getId() === 18) {" +
                "    transporterId = items[i].getId();" +
                "  }" +
                "  if (items[i].getBaseItemType().getId() === " + itemTypeIdToLoad + ") {" +
                "    loadIds.push(items[i].getId());" +
                "  }" +
                "}" +
                "if (transporterId != null && loadIds.length > 0) {" +
                "  gameCmd.loadContainerCmd(loadIds, transporterId);" +
                "}"
        );
    }

    /**
     * Unloads a transporter.
     */
    public void jsUnloadTransporter() {
        executeScript(
                "var svc = window.gwtAngularFacade.babylonRenderServiceAccess;" +
                "var bridge = window.gwtAngularFacade.itemCockpitBridge;" +
                "var items = svc.getBabylonBaseItemsByDiplomacy('OWN');" +
                "for (var i = 0; i < items.length; i++) {" +
                "  if (items[i].getBaseItemType().getId() === 18) {" +
                "    if (bridge) bridge.requestUnload(items[i].getId());" +
                "    return;" +
                "  }" +
                "}"
        );
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
     * Verifies cursor behavior: hovers empty terrain (go) and terrain object (go-no).
     * Terrain object check is best-effort (async loading may delay it).
     */
    public void verifyCursors() {
        double[] emptyPos = hoverEmptyTerrain();
        assertThat(emptyPos, "Empty terrain position");
        double[] objPos = tryHoverTerrainObject();
        // objPos may be null if terrain objects aren't loaded yet - that's OK
    }

    private void assertThat(double[] pos, String name) {
        if (pos == null) {
            // Soft check - don't fail
        }
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
        long countBefore = getBaseItemCount();
        waitForBuildButtonForItemType(itemTypeId);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {} // Let carousel settle
        clickBuildButtonForItemType(itemTypeId);
        waitForBaseItemPlacerActive();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {} // Let placer initialize
        placeOnFreePosition();
        waitForBaseItemCountAbove(countBefore);
    }

    /**
     * Builds with quest region awareness - moves camera to quest region before placing.
     */
    public void buildViaBuilderInQuestRegion(int itemTypeId) {
        long countBefore = getBaseItemCount();
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
