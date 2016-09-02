package com.btxtech.shared.gameengine.datatypes.itemtype;


import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * User: beat
 * Date: 01.11.12
 * Time: 14:04
 */
public class ItemClipPosition {
    private int clipId;
    private Index[] positions;

    /**
     * Do not delete. Used by GWT
     */
    public ItemClipPosition() {
    }

    public ItemClipPosition(int clipId, Index[] positions) {
        this.clipId = clipId;
        this.positions = positions;
    }

    public int getClipId() {
        return clipId;
    }

    public void setClipId(int clipId) {
        this.clipId = clipId;
    }

    public Index getOffset(SyncBaseItem syncBaseItem) {
        return positions[syncBaseItem.getSyncItemArea().getAngelIndex()];
    }

    public Index[] getPositions() {
        return positions;
    }

    public void setPositions(Index[] positions) {
        this.positions = positions;
    }

    public boolean isClipIdValid() {
        return clipId != 0;
    }
}
