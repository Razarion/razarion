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
    public void onMove(double xTerrainPosition, double yTerrainPosition) {
        DecimalPosition position = new DecimalPosition(xTerrainPosition, yTerrainPosition);
        try {
            baseItemPlacerChecker.check(position);
            setupErrorText();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "BaseItemPlacer.onMove() " + position, e);
        }
    }

    @SuppressWarnings("unused") // Called by Angular
    public void onPlace(double xTerrainPosition, double yTerrainPosition) {
        DecimalPosition position = new DecimalPosition(xTerrainPosition, yTerrainPosition);
        try {
            baseItemPlacerChecker.check(position);
            setupErrorText();
            placeCallback.accept(position);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "BaseItemPlacer.onPlace() " + position, e);
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
