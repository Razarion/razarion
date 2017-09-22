package com.btxtech.server.persistence.level;

import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * on 20.09.2017.
 */
@Entity
@Table(name = "LEVEL_UNLOCK")
public class LevelUnlockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity baseItemType;
    private int baseItemTypeCount;
    private int crystalCost;

    public Integer getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public int getCrystalCost() {
        return crystalCost;
    }

    public void setCrystalCost(int crystalCost) {
        this.crystalCost = crystalCost;
    }

    public BaseItemTypeEntity getBaseItemType() {
        return baseItemType;
    }

    public void setBaseItemType(BaseItemTypeEntity baseItemType) {
        this.baseItemType = baseItemType;
    }

    public int getBaseItemTypeCount() {
        return baseItemTypeCount;
    }

    public void setBaseItemTypeCount(int count) {
        this.baseItemTypeCount = count;
    }

    public LevelUnlockConfig toLevelUnlockConfig() {
        LevelUnlockConfig levelUnlockConfig = new LevelUnlockConfig().setId(id).setInternalName(internalName).setBaseItemTypeCount(baseItemTypeCount).setCrystalCost(crystalCost);
        if (baseItemType != null) {
            levelUnlockConfig.setBaseItemType(baseItemType.getId());
        }
        return levelUnlockConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LevelUnlockEntity that = (LevelUnlockEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
