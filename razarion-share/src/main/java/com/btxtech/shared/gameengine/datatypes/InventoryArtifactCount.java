package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import jsinterop.annotations.JsType;
import org.teavm.flavour.json.JsonPersistable;

/**
 * A required amount of a given {@link InventoryArtifact}. Used as the assemble
 * cost of an {@link InventoryItem} in the workshop. Ported from the legacy
 * controltheland project (DbInventoryArtifactCount).
 */
@JsType
@JsonPersistable
public class InventoryArtifactCount {
    @CollectionReference(CollectionReferenceType.INVENTORY_ARTIFACT)
    private Integer inventoryArtifactId;
    private int count;

    public Integer getInventoryArtifactId() {
        return inventoryArtifactId;
    }

    public void setInventoryArtifactId(Integer inventoryArtifactId) {
        this.inventoryArtifactId = inventoryArtifactId;
    }

    public InventoryArtifactCount inventoryArtifactId(Integer inventoryArtifactId) {
        setInventoryArtifactId(inventoryArtifactId);
        return this;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public InventoryArtifactCount count(int count) {
        setCount(count);
        return this;
    }
}
