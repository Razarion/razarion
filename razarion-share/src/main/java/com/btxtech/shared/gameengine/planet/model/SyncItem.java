package com.btxtech.shared.gameengine.planet.model;


import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;

/**
 * User: Razarion contributors
 * Date: 18.11.2009
 * Time: 14:17:17
 */
public abstract class SyncItem {
    private int id;
    // Own states
    private ItemType itemType;
    private AbstractSyncPhysical abstractSyncPhysical;

    public void init(int id, ItemType itemType) {
        this.id = id;
        this.itemType = itemType;
    }

    public void setAbstractSyncPhysical(AbstractSyncPhysical abstractSyncPhysical) {
        this.abstractSyncPhysical = abstractSyncPhysical;
    }

    public int getId() {
        return id;
    }

    public ItemType getItemType() {
        return itemType;
    }

    protected void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    @Deprecated
    public abstract boolean isAlive();

    public AbstractSyncPhysical getAbstractSyncPhysical() {
        return abstractSyncPhysical;
    }

    public SyncPhysicalMovable getSyncPhysicalMovable() {
        if (abstractSyncPhysical instanceof SyncPhysicalMovable) {
            return (SyncPhysicalMovable) abstractSyncPhysical;
        }
        throw new IllegalStateException("SyncItem does not have a SyncPhysicalMovable: " + this);
    }

    @Override
    public String toString() {
        if (abstractSyncPhysical != null) {
            return "SyncItem: id=" + id + "|" + itemType + "|" + abstractSyncPhysical.getPosition();
        } else {
            return "SyncItem: id=" + id + "|" + itemType + "|no syncPhysicalArea";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SyncItem)) {
            return false;
        }

        SyncItem syncItem = (SyncItem) o;
        return id == syncItem.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
