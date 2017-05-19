package com.btxtech.shared.gameengine.planet.model;


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoundingBox;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

import java.util.logging.Logger;

/**
 * User: beat
 * Date: 16.08.2011
 * Time: 21:13:43
 */


@Deprecated
// DELETE THIS
// replaced by SyncPhysicalArea
public class SyncItemArea {
    private DecimalPosition position;
    private double angel = 0;
    private SyncItem syncItem;
    private BoundingBox boundingBox;
    private Logger log = Logger.getLogger(SyncItemArea.class.getName());

    public SyncItemArea(BoundingBox boundingBox, DecimalPosition position) {
        this.boundingBox = boundingBox;
        setPosition(position);
    }

    public SyncItemArea(SyncItemArea syncItemArea) {
        position = new DecimalPosition(syncItemArea.position);
        angel = syncItemArea.angel;
        boundingBox = syncItemArea.boundingBox;
    }

    public DecimalPosition getPosition() {
        throw new UnsupportedOperationException();
//        if (position != null) {
//            return position.toIndex();
//        } else {
//            return null;
//        }
    }

    public Index getTopLeftFromImagePosition() {
        throw new UnsupportedOperationException();
        // return toIndex().sub(syncItem.getItemType().getItemTypeSpriteMap().getMiddleFromImage());
    }

    public DecimalPosition getDecimalPosition() {
        return position;
    }

    private void checkPosition() {
        if (position != null) {
            if (position.getX() < 0 || position.getY() < 0) {
                throw new IllegalStateException("Position is not allowed to be negative: " + position + " SyncItem: " + syncItem);
            }
        }
    }

    public void setPosition(DecimalPosition position) {
        throw new UnsupportedOperationException();
//        setPositionNoCheck(position);
//        checkPosition();
//        if (syncItem != null) {
//            // TODO syncItem.fireItemChanged(SyncItemListener.Change.POSITION, null);
//        }
    }

    public void setPositionNoCheck(Index position) {
        if (position != null) {
            this.position = new DecimalPosition(position);
        } else {
            this.position = null;
        }
    }

    public void setDecimalPosition(DecimalPosition decimalPoint) {
        position = decimalPoint;
        checkPosition();
        if (syncItem != null) {
            // TODO syncItem.fireItemChanged(SyncItemListener.Change.POSITION, null);
        }
    }

    public double getAngel() {
        return angel;
    }


    public int getAngelIndex() {
        return boundingBox.angelToAngelIndex(angel);
    }

    public void setAngel(double angel) {
        this.angel = angel;
        if (syncItem != null) {
            // TODO syncItem.fireItemChanged(SyncItemListener.Change.ANGEL, null);
        }
    }

    public void synchronize(SyncBaseItemInfo syncItemInfo) {
    }

    public void fillSyncItemInfo(SyncBaseItemInfo syncItemInfo) {
    }

    public boolean hasPosition() {
        return position != null;
    }

    public void turnTo(double angel) {
        if (angel != this.angel) {
            this.angel = angel;
            if (syncItem != null) {
                // TODO syncItem.fireItemChanged(SyncItemListener.Change.ANGEL, null);
            }
        }
    }

    public void turnTo(DecimalPosition destination) {
        throw new UnsupportedOperationException();
//        if (destination.equals(toIndex())) {
//            return;
//        }
//
//        turnTo(getTurnToAngel(destination));
    }

    public void turnTo(SyncItem target) {
        turnTo(target.getSyncItemArea());
    }

    public void turnTo(SyncItemArea target) {
        throw new UnsupportedOperationException();
        // turnTo(target.toIndex());
    }

    public double getTurnToAngel(Index destination) {
        if (destination.equals(getPosition())) {
            return getBoundingBox().getCosmeticAngel();
        }

        throw new UnsupportedOperationException();
        // return toIndex().getAngleToNord(destination);
    }

    public double getTurnToAngel(SyncItemArea destination) {
        throw new UnsupportedOperationException();
        // return getTurnToAngel(destination.toIndex());
    }

    public boolean contains(SyncItemArea syncItemArea) {
        if (!hasPosition()) {
            return false;
        }
        if (!syncItemArea.hasPosition()) {
            return false;
        }

        // Increase performance
        return getPosition().getDistance(syncItemArea.getPosition()) <= (getBoundingBox().getRadius() + syncItemArea.getBoundingBox().getRadius());
    }

    public boolean contains(Index position) {
        return getPosition().getDistance(position) <= getBoundingBox().getRadius();
    }

    public boolean contains(SyncItem syncItem) {
        return contains(syncItem.getSyncItemArea());
    }

    /**
     * Move this SyncItemArea to the given position and check contains
     *
     * @param syncItem        to check against
     * @param positionToCheck position to move this to
     * @param angel           angel move to. If null -> 0
     * @return true if contains
     */
    public boolean contains(SyncItem syncItem, Index positionToCheck, Double angel) {
        if (angel == null) {
            angel = 0.0;
        }
        return getBoundingBox().createSyntheticSyncItemArea(positionToCheck, angel).contains(syncItem);
    }

    /**
     * Move this SyncItemArea to the given position and
     *
     * @param syncItem        to check against
     * @param positionToCheck position to move this to
     * @return true if contains
     */
    public boolean contains(SyncItem syncItem, Index positionToCheck) {
        return getBoundingBox().createSyntheticSyncItemArea(positionToCheck).contains(syncItem);
    }

    /**
     * Check if this SyncItemArea will contains the given bounding box at the given position. The bounding box
     * is moved to the given position
     *
     * @param boundingBox     bounding box
     * @param positionToCheck position to check
     * @return true if contains
     */
    public boolean contains(BoundingBox boundingBox, Index positionToCheck) {
        return contains(boundingBox.createSyntheticSyncItemArea(positionToCheck));
    }

    public boolean contains(Rectangle rectangle) {
        throw new UnsupportedOperationException();
//        if (!hasPosition()) {
//            return false;
//        }
//        if (rectangle.contains(toIndex())) {
//            return true;
//        }
//        Rectangle biggestScope = Rectangle.generateRectangleFromMiddlePoint(rectangle.center(),
//                rectangle.width() + boundingBox.getDiameter(),
//                rectangle.height() + boundingBox.getDiameter());
//        if (!biggestScope.containsExclusive(new DecimalPosition(toIndex()))) {
//            return false;
//        }
//        return rectangle.getNearestPoint(new DecimalPosition(toIndex())).getDistance(toIndex()) <= boundingBox.getRadius();
    }

    public Rectangle generateCoveringRectangle() {
        throw new UnsupportedOperationException();
        // return Rectangle.generateRectangleFromMiddlePoint(toIndex(), boundingBox.getDiameter(), boundingBox.getDiameter());
    }

    public boolean positionReached(DecimalPosition destination) {
        return getPosition().equals(destination);
    }

    public double getDistance(Index position) {
        if (contains(position)) {
            return 0;
        }
        return getPosition().getDistance(position) - getBoundingBox().getRadius();
    }

    public double getDistance(SyncItem syncItem) throws TargetHasNoPositionException {
        return getDistance(syncItem.getSyncItemArea());
    }

    public int getDistanceRounded(SyncItem syncItem) throws TargetHasNoPositionException {
        return (int) Math.round(getDistance(syncItem.getSyncItemArea()));
    }

    public int getDistanceRounded(Index position) {
        return (int) Math.round(getDistance(position));
    }

    public int getDistanceRounded(SyncItemArea syncItemArea) throws TargetHasNoPositionException {
        return (int) Math.round(getDistance(syncItemArea));
    }

    public double getDistance(SyncItemArea syncItemArea) throws TargetHasNoPositionException {
        if (!syncItemArea.hasPosition()) {
            throw new TargetHasNoPositionException(syncItem);
        }
        if (contains(syncItemArea)) {
            return 0;
        }
        throw new UnsupportedOperationException();
        // return toIndex().getDistanceDouble(syncItemArea.toIndex()) - getBoundingBox().getRadius() - syncItemArea.getBoundingBox().getRadius();
    }

    public boolean isInRange(int range, SyncItem target) throws TargetHasNoPositionException {
        return range >= getDistanceRounded(target);
    }

    public boolean isInRange(int range, Index position) {
        return range >= getDistanceRounded(position);
    }

    public boolean isInRange(int range, SyncItemArea syncItemArea) throws TargetHasNoPositionException {
        return range >= getDistanceRounded(syncItemArea);
    }

    public void setCosmeticsAngel() {
        setAngel(getBoundingBox().getCosmeticAngel());
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    @Override
    public String toString() {
        return " SyncItemArea: " + getPosition() + " angel: " + angel + " " + boundingBox;
    }
}
