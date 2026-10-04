package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Razarion contributors
 * Date: 02.05.2013
 * Time: 18:02
 */

public class BaseItemPlacer {
    /** Grid of the opening-spot search: a crescent of dry ground a few units wide is not missed. */
    private static final double OPEN_SEARCH_STEP = 2.0;
    /** Largest area searched for an opening spot: a ship's reach is 40 x 40, a start region is not. */
    private static final double MAX_OPEN_SEARCH_AREA = 100.0 * 100.0;
    private final Logger logger = Logger.getLogger(BaseItemPlacer.class.getName());
    private final BaseItemPlacerChecker baseItemPlacerChecker;
    private final ItemTypeService itemTypeService;
    private boolean canBeCanceled;
    private Consumer<DecimalPosition> placeCallback;
    private Runnable cancelCallback;
    private BaseItemType baseItemType;
    private String errorText;
    private String lastLoggedErrorText;
    /** What the caller wants said when the spot is outside its allowed area; null = the general wording. */
    private String allowedAreaText;
    /** A valid spot to open on, see {@link #findOpenPosition}; null = the screen centre. */
    private DecimalPosition openPosition;

    @Inject
    public BaseItemPlacer(ItemTypeService itemTypeService, BaseItemPlacerChecker baseItemPlacerChecker) {
        this.itemTypeService = itemTypeService;
        this.baseItemPlacerChecker = baseItemPlacerChecker;
    }

    public BaseItemPlacer init(BaseItemPlacerConfig baseItemPlacerConfig, boolean canBeCanceled, Consumer<DecimalPosition> placeCallback, Runnable cancelCallback) {
        baseItemType = itemTypeService.getBaseItemType(baseItemPlacerConfig.getBaseItemTypeId());
        allowedAreaText = baseItemPlacerConfig.getAllowedAreaText();
        this.canBeCanceled = canBeCanceled;
        this.placeCallback = placeCallback;
        this.cancelCallback = cancelCallback;
        baseItemPlacerChecker.init(baseItemType, baseItemPlacerConfig);
//        if (baseItemPlacerConfig.getSuggestedPosition() != null) {
//            onMove(new Vertex(baseItemPlacerConfig.getSuggestedPosition(), 0));
//        }
        openPosition = null;
        // The search area when the caller gives one - the allowed area can be far too large to probe
        // (a quest region along a whole coast); the checker still refuses spots outside it.
        PlaceConfig searchArea = baseItemPlacerConfig.getOpenSearchArea() != null
                ? baseItemPlacerConfig.getOpenSearchArea()
                : baseItemPlacerConfig.getAllowedArea();
        if (baseItemPlacerConfig.isOpenInAllowedArea() && searchArea != null) {
            try {
                openPosition = findOpenPosition(searchArea, baseItemPlacerConfig.getPreferredArea());
            } catch (Throwable t) {
                // Without it the placer opens at the screen centre, as it always did.
                logger.warning("BaseItemPlacer.findOpenPosition() failed: " + t.getMessage());
            }
        }
        return this;
    }

    /**
     * Where the placer should open: a valid spot in the allowed area, one in the preferred area if
     * there is any, and of those the one nearest to the area's centre. Null when nothing fits.
     * <p>
     * For the unload placer, whose allowed area is the reach of the ship. On PROD a ship at the
     * Phase 2 coast (quest 392, 2026-09-27) had 33 valid spots on a 2-unit grid out of 375 in
     * reach: a thin crescent of dry ground. The wet beach in front of the ship looks like land and
     * is water, and the land the quest marks is mostly out of reach - "Terrain not suitable here"
     * and "Too far from the Transporter" in turns, until the player gave up.
     * <p>
     * Only ground the client has on screen can be judged (TerrainUiService#isTerrainFree reads the
     * displayed tiles); a ship off screen gets no opening spot, and the placer behaves as before.
     */
    private DecimalPosition findOpenPosition(PlaceConfig allowedArea, PlaceConfig preferredArea) {
        Rectangle2D aabb = allowedArea.toAabb();
        if (aabb == null || aabb.width() * aabb.height() > MAX_OPEN_SEARCH_AREA) {
            return null;
        }
        DecimalPosition center = allowedArea.getPosition() != null
                ? allowedArea.getPosition()
                : new DecimalPosition(aabb.startX() + aabb.width() / 2.0, aabb.startY() + aabb.height() / 2.0);
        DecimalPosition best = null;
        double bestDistance = Double.MAX_VALUE;
        boolean bestPreferred = false;
        for (double x = aabb.startX(); x <= aabb.startX() + aabb.width(); x += OPEN_SEARCH_STEP) {
            for (double y = aabb.startY(); y <= aabb.startY() + aabb.height(); y += OPEN_SEARCH_STEP) {
                DecimalPosition position = new DecimalPosition(x, y);
                baseItemPlacerChecker.check(position);
                if (!baseItemPlacerChecker.isPositionValid()) {
                    continue;
                }
                boolean preferred = preferredArea != null && preferredArea.checkInside(position);
                double distance = position.getDistance(center);
                if ((preferred && !bestPreferred) || (preferred == bestPreferred && distance < bestDistance)) {
                    best = position;
                    bestDistance = distance;
                    bestPreferred = preferred;
                }
            }
        }
        return best;
    }

    public DecimalPosition getOpenPosition() {
        return openPosition;
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getEnemyFreeRadius() {
        return baseItemPlacerChecker.getEnemyFreeRadius();
    }

    @SuppressWarnings("unused") // Called by Angular
    /**
     * Catches Throwable, not Exception, and that difference is the whole defect.
     * <p>
     * In TeaVM WASM-GC a null dereference is not a NullPointerException but a trap that arrives as
     * an Error, so the previous catch(Exception) let it straight through. From here it escaped the
     * placer, the scene that activates it, the worker message dispatch, and finally the tick pull
     * loop - which is how one bad check produced a game that rendered terrain, moved its camera,
     * and never showed a unit or a deploy dialog again. Measured on PROD on 2026-08-31:
     * "dispatch INITIAL_SLAVE_SYNCHRONIZED_NO_BASE: runScene.run(script: Multiplayer Planet
     * viewfield): runScene.run(Multiplayer wait for base created): dereferencing a null pointer".
     * <p>
     * Proven rather than assumed: the same trap was caught the moment runScene wrapped it in
     * catch(Throwable), while this catch(Exception) had been letting it past for weeks.
     * <p>
     * A check that cannot be completed means the position is not known to be good, so it is
     * refused - the ghost turns red and the player moves it elsewhere. That is recoverable.
     * Letting it through is not.
     */
    public void onMove(double xTerrainPosition, double yTerrainPosition) {
        // A method whose whole job is to answer "may I build here" has no business taking a
        // session down, so all of it is guarded - including the construction of the position.
        //
        // One thing here is still not understood. On 2026-08-31 a WASM trap from
        // UiTerrainTile.getTerrainType passed straight through this catch, and the log line below
        // never appeared, although the debug build put onMove squarely in the stack and the same
        // catch(Throwable) in GameUiControl.runScene held a trap of the same kind twice in the
        // same session. The null itself is fixed at its source; this remains as a guard whose
        // reliability against a trap is unproven.
        try {
            DecimalPosition position = new DecimalPosition(xTerrainPosition, yTerrainPosition);
            baseItemPlacerChecker.check(position);
            setupErrorText();
        } catch (Throwable t) {
            errorText = "Can not check this position";
            // The message only. Handing a WASM trap to a formatter is one more thing that can
            // trap, and it would do so from inside the handler for the first one.
            logger.severe("BaseItemPlacer.onMove(" + xTerrainPosition + ", " + yTerrainPosition
                    + ") failed: " + t.getMessage());
        }
    }

    @SuppressWarnings("unused") // Called by Angular
    public void onPlace(double xTerrainPosition, double yTerrainPosition) {
        DecimalPosition position = new DecimalPosition(xTerrainPosition, yTerrainPosition);
        try {
            baseItemPlacerChecker.check(position);
            setupErrorText();
            placeCallback.accept(position);
        } catch (Throwable t) {
            // Same reason as onMove: a WASM trap is an Error, not an Exception, and this is the
            // tap that actually places the base - the one moment in the whole funnel that must
            // not take the session down with it.
            errorText = "Can not check this position";
            logger.severe("BaseItemPlacer.onPlace() " + position + " failed: " + t.getMessage());
        }
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean isPositionValid() {
        return baseItemPlacerChecker.isPositionValid();
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean hasRallyPoint() {
        return baseItemPlacerChecker.hasRallyPoint();
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getRallyOffsetX() {
        return baseItemPlacerChecker.getRelativeRallyPosition() != null ? baseItemPlacerChecker.getRelativeRallyPosition().getX() : 0;
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getRallyOffsetY() {
        return baseItemPlacerChecker.getRelativeRallyPosition() != null ? baseItemPlacerChecker.getRelativeRallyPosition().getY() : 0;
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getRallyRadius() {
        return baseItemPlacerChecker.getRallyRadius();
    }

    public String getErrorText() {
        return errorText;
    }

    Collection<DecimalPosition> setupAbsolutePositions(DecimalPosition terrainPosition) {
        return baseItemPlacerChecker.setupAbsolutePositions(terrainPosition);
    }

    DecimalPosition getAbsoluteRallyPosition(DecimalPosition terrainPosition) {
        return baseItemPlacerChecker.getAbsoluteRallyPosition(terrainPosition);
    }

    @SuppressWarnings("unused") // Called by Angular
    public Integer getModel3DId() {
        return baseItemType.getModel3DId();
    }

    @SuppressWarnings("unused") // Called by Angular
    public Collection<DecimalPosition> getRelativeItemPositions() {
        return baseItemPlacerChecker.getRelativeItemPositions();
    }

    @SuppressWarnings("unused") // Called by Angular
    public Integer getSpawnAudioId() {
        return baseItemType.getSpawnAudioId();
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean isPlayBuildSound() {
        return canBeCanceled;
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean isCanBeCanceled() {
        return canBeCanceled;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void cancel() {
        if (canBeCanceled) {
            cancelCallback.run();
        }
    }

    /**
     * Names the first failing check, in the order {@link BaseItemPlacerChecker#check} evaluates them -
     * the later ones are false as a consequence of the earlier one, so the first is the actual cause.
     * The rally check is independent and therefore reported last.
     * <p>
     * Shown to the player instead of the generic "move mouse to find free position". Without it a red
     * placer gives no clue at all: on 2026-08-01 a player spent his last 11 minutes failing to rebuild
     * a factory and neither he nor the logs could say which of the six conditions was blocking him.
     */
    private void setupErrorText() {
        if (!baseItemPlacerChecker.isAllowedAreaOk()) {
            errorText = allowedAreaText != null ? allowedAreaText : "Outside the allowed area";
        } else if (!baseItemPlacerChecker.isEnemiesOk()) {
            errorText = "Enemy too near";
        } else if (!baseItemPlacerChecker.isItemsOk()) {
            errorText = "Blocked by another item";
        } else if (!baseItemPlacerChecker.isResourcesOk()) {
            errorText = "Can not build on a razarion field";
        } else if (!baseItemPlacerChecker.isTerrainOk()) {
            // The Dockyard is the first building in the game that stands in the water, and "not
            // suitable" never said which ground would be: on PROD 7 of 13 players who stayed on quest
            // 386 got this answer on land and never placed one (2026-09-30).
            errorText = isWaterBuilding() ? "Build it on the water" : "Terrain not suitable here";
        } else if (!baseItemPlacerChecker.isRallyTerrainOk()) {
            errorText = "Needs free ground to the east for the rally point";
        } else {
            errorText = null;
        }
    }

    private boolean isWaterBuilding() {
        return baseItemType != null
                && baseItemType.getPhysicalAreaConfig() != null
                && baseItemType.getPhysicalAreaConfig().getTerrainType() == TerrainType.WATER;
    }

    /**
     * Called when the player clicks on a red position. The presenter swallows that click, so without
     * this the attempt leaves no trace whatsoever - not in the UI and not in the remote log.
     * <p>
     * Logged at WARNING because that is the level the Angular console hook forwards to the server.
     * Repeated clicks with an unchanged cause are dropped so a frustrated player does not flood the log.
     */
    @SuppressWarnings("unused") // Called by Angular
    public void onInvalidPlaceAttempt() {
        String reason = errorText != null ? errorText : "unknown";
        if (reason.equals(lastLoggedErrorText)) {
            return;
        }
        lastLoggedErrorText = reason;
        logger.warning("BaseItemPlacer rejected " + baseItemType.getInternalName() + ": " + reason
                + " [allowedArea=" + baseItemPlacerChecker.isAllowedAreaOk()
                + " enemies=" + baseItemPlacerChecker.isEnemiesOk()
                + " items=" + baseItemPlacerChecker.isItemsOk()
                + " resources=" + baseItemPlacerChecker.isResourcesOk()
                + " terrain=" + baseItemPlacerChecker.isTerrainOk()
                + " rallyTerrain=" + baseItemPlacerChecker.isRallyTerrainOk() + "]");
    }
}
