package com.btxtech.server.persistence.history;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Beat
 * on 18.09.2017.
 */
@Entity
@Table(name = "HISTORY_INVENTORY")
public class InventoryHistoryEntry {
    public enum Type {
        BOX_PICKED,
        INVENTORY_ITEM_USED
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, columnDefinition = "DATETIME(3)")
    private Date timeStamp;
    private int humanPlayerIdEntityId;
    @Enumerated(EnumType.STRING)
    private Type type;
    private Integer crystals;
    private Integer inventoryItemId;
    private String inventoryItemName;

    public int getHumanPlayerIdEntityId() {
        return humanPlayerIdEntityId;
    }

    public void setHumanPlayerIdEntityId(int humanPlayerIdEntityId) {
        this.humanPlayerIdEntityId = humanPlayerIdEntityId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getCrystals() {
        return crystals;
    }

    public void setCrystals(Integer crystals) {
        this.crystals = crystals;
    }

    public Integer getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(Integer inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public String getInventoryItemName() {
        return inventoryItemName;
    }

    public void setInventoryItemName(String inventoryItemName) {
        this.inventoryItemName = inventoryItemName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InventoryHistoryEntry that = (InventoryHistoryEntry) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
