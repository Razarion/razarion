package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 02.05.2013
 * Time: 18:02
 */

public class BaseItemPlacer {
    private final Logger logger = Logger.getLogger(BaseItemPlacer.class.getName());
    private final BaseItemPlacerChecker baseItemPlacerChecker;
    private final ItemTypeService itemTypeService;
    private boolean canBeCanceled;
    private Consumer<DecimalPosition> placeCallback;
    private Runnable cancelCallback;
    private BaseItemType baseItemType;
    private String errorText;
    private String lastLoggedErrorText;

    @Inject
    public BaseItemPlacer(ItemTypeService itemTypeService, BaseItemPlacerChecker baseItemPlacerChecker) {
        this.itemTypeService = itemTypeService;
        this.baseItemPlacerChecker = baseItemPlacerChecker;
    }

    public BaseItemPlacer init(BaseItemPlacerConfig baseItemPlacerConfig, boolean canBeCanceled, Consumer<DecimalPosition> placeCallback, Runnable cancelCallback) {
        baseItemType = itemTypeService.getBaseItemType(baseItemPlacerConfig.getBaseItemTypeId());
        this.canBeCanceled = canBeCanceled;
        this.placeCallback = placeCallback;
        this.cancelCallback = cancelCallback;
        baseItemPlacerChecker.init(baseItemType, baseItemPlacerConfig);
//        if (baseItemPlacerConfig.getSuggestedPosition() != null) {
//            onMove(new Vertex(baseItemPlacerConfig.getSuggestedPosition(), 0));
//        }
        return this;
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
            errorText = "Outside the allowed area";
        } else if (!baseItemPlacerChecker.isEnemiesOk()) {
            errorText = "Enemy too near";
        } else if (!baseItemPlacerChecker.isItemsOk()) {
            errorText = "Blocked by another item";
        } else if (!baseItemPlacerChecker.isResourcesOk()) {
            errorText = "Can not build on a razarion field";
        } else if (!baseItemPlacerChecker.isTerrainOk()) {
            errorText = "Terrain not suitable here";
        } else if (!baseItemPlacerChecker.isRallyTerrainOk()) {
            errorText = "Needs free ground to the east for the rally point";
        } else {
            errorText = null;
        }
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
