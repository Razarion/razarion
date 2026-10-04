package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * User: Razarion contributors
 * Date: 01.05.13
 * Time: 13:00
 */
public class BaseItemPlacerConfig {
    private DecimalPosition suggestedPosition;
    @CollectionReference(CollectionReferenceType.BASE_ITEM)
    private int baseItemTypeId;
    private int baseItemCount;
    private Double enemyFreeRadius;
    private PlaceConfig allowedArea;
    /**
     * What to tell the player when the spot is outside {@link #allowedArea}, when "outside the
     * allowed area" does not say anything they can act on. The unload placer sets it: its area is
     * the reach of the ship they are standing next to, and only the caller knows that.
     */
    private String allowedAreaText;
    /**
     * Open the placer on a valid spot inside {@link #allowedArea} instead of at the screen centre.
     * Only for a small area: the whole area is probed once on opening. The unload placer sets it -
     * its area is a ship's reach, and the ground in it that takes a unit is often a thin strip.
     */
    private boolean openInAllowedArea;
    /**
     * With {@link #openInAllowedArea}: where in the allowed area the opening spot should rather be,
     * e.g. the region the active quest counts. Null = anywhere in the allowed area.
     */
    private PlaceConfig preferredArea;
    /**
     * With {@link #openInAllowedArea}: where to look for the opening spot, in place of
     * {@link #allowedArea} - which can be far too large to probe - and without restricting where the
     * player may place. The build placer of a quest that counts a region sets it around the spot the
     * camera has just travelled to (quest 386, 2026-10-02); the region itself is the allowed area.
     */
    private PlaceConfig openSearchArea;

    public DecimalPosition getSuggestedPosition() {
        return suggestedPosition;
    }

    public void setSuggestedPosition(DecimalPosition suggestedPosition) {
        this.suggestedPosition = suggestedPosition;
    }

    public int getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public void setBaseItemTypeId(int baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
    }

    public int getBaseItemCount() {
        return baseItemCount;
    }

    public void setBaseItemCount(int baseItemCount) {
        this.baseItemCount = baseItemCount;
    }

    public Double getEnemyFreeRadius() {
        return enemyFreeRadius;
    }

    public void setEnemyFreeRadius(Double enemyFreeRadius) {
        this.enemyFreeRadius = enemyFreeRadius;
    }

    public PlaceConfig getAllowedArea() {
        return allowedArea;
    }

    public void setAllowedArea(PlaceConfig allowedArea) {
        this.allowedArea = allowedArea;
    }

    public BaseItemPlacerConfig suggestedPosition(DecimalPosition suggestedPosition) {
        setSuggestedPosition(suggestedPosition);
        return this;
    }

    public BaseItemPlacerConfig baseItemTypeId(int baseItemTypeId) {
        setBaseItemTypeId(baseItemTypeId);
        return this;
    }

    public BaseItemPlacerConfig baseItemCount(int baseItemCount) {
        setBaseItemCount(baseItemCount);
        return this;
    }

    public BaseItemPlacerConfig enemyFreeRadius(Double enemyFreeRadius) {
        setEnemyFreeRadius(enemyFreeRadius);
        return this;
    }

    public BaseItemPlacerConfig allowedArea(PlaceConfig allowedArea) {
        setAllowedArea(allowedArea);
        return this;
    }

    public String getAllowedAreaText() {
        return allowedAreaText;
    }

    public void setAllowedAreaText(String allowedAreaText) {
        this.allowedAreaText = allowedAreaText;
    }

    public BaseItemPlacerConfig allowedAreaText(String allowedAreaText) {
        setAllowedAreaText(allowedAreaText);
        return this;
    }

    public boolean isOpenInAllowedArea() {
        return openInAllowedArea;
    }

    public void setOpenInAllowedArea(boolean openInAllowedArea) {
        this.openInAllowedArea = openInAllowedArea;
    }

    public BaseItemPlacerConfig openInAllowedArea(boolean openInAllowedArea) {
        setOpenInAllowedArea(openInAllowedArea);
        return this;
    }

    public PlaceConfig getPreferredArea() {
        return preferredArea;
    }

    public void setPreferredArea(PlaceConfig preferredArea) {
        this.preferredArea = preferredArea;
    }

    public BaseItemPlacerConfig preferredArea(PlaceConfig preferredArea) {
        setPreferredArea(preferredArea);
        return this;
    }

    public PlaceConfig getOpenSearchArea() {
        return openSearchArea;
    }

    public void setOpenSearchArea(PlaceConfig openSearchArea) {
        this.openSearchArea = openSearchArea;
    }

    public BaseItemPlacerConfig openSearchArea(PlaceConfig openSearchArea) {
        setOpenSearchArea(openSearchArea);
        return this;
    }
}
