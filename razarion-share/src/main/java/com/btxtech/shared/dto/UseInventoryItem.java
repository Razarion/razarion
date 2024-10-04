package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import org.dominokit.jackson.annotation.JSONMapper;

import java.util.List;

/**
 * Created by Beat
 * on 18.09.2017.
 */
@JSONMapper
public class UseInventoryItem {
    private int inventoryId;
    private List<DecimalPosition> positions;

    public int getInventoryId() {
        return inventoryId;
    }

    public UseInventoryItem setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
        return this;
    }

    public List<DecimalPosition> getPositions() {
        return positions;
    }

    public UseInventoryItem setPositions(List<DecimalPosition> positions) {
        this.positions = positions;
        return this;
    }
}
