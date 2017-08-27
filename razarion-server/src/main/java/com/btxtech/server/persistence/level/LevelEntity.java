package com.btxtech.server.persistence.level;

import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 05.05.2017.
 */
@Entity
@Table(name = "LEVEL")
public class LevelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int number;
    private int xp2LevelUp;
    @ElementCollection
    @MapKeyJoinColumn(name = "baseItemTypeEntityId")
    @CollectionTable(name = "LEVEL_LIMITATION")
    private Map<BaseItemTypeEntity, Integer> itemTypeLimitation;

    public Integer getId() {
        return id;
    }

    public LevelConfig toLevelConfig() {
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        if (this.itemTypeLimitation != null) {
            for (Map.Entry<BaseItemTypeEntity, Integer> entry : this.itemTypeLimitation.entrySet()) {
                itemTypeLimitation.put(entry.getKey().getId(), entry.getValue());
            }
        }
        return new LevelConfig().setLevelId(id).setNumber(number).setXp2LevelUp(xp2LevelUp).setItemTypeLimitation(itemTypeLimitation);
    }

    public void fromLevelConfig(LevelConfig levelConfig, Map<BaseItemTypeEntity, Integer> itemTypeLimitation) {
        this.number = levelConfig.getNumber();
        this.xp2LevelUp = levelConfig.getXp2LevelUp();
        if (this.itemTypeLimitation == null) {
            this.itemTypeLimitation = new HashMap<>();
        }
        this.itemTypeLimitation.clear();
        this.itemTypeLimitation.putAll(itemTypeLimitation);
    }

    public int getNumber() {
        return number;
    }

    public int getXp2LevelUp() {
        return xp2LevelUp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LevelEntity that = (LevelEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
