package com.btxtech.server.persistence.bot;

import com.btxtech.server.persistence.PlaceConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Entity
@Table(name = "BOT_CONFIG_BOT_ITEM")
public class BotItemConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne
    private BaseItemTypeEntity baseItemTypeEntity;
    private int count;
    private boolean createDirectly;
    private boolean noSpawn;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PlaceConfigEntity place;
    private double angle;
    private boolean moveRealmIfIdle;
    private Integer idleTtl;
    private boolean noRebuild;
    private Integer rePopTime;

    public BotItemConfig toBotItemConfig() {
        BotItemConfig botItemConfig = new BotItemConfig().setCount(count).setCreateDirectly(createDirectly).setAngle(angle).setMoveRealmIfIdle(moveRealmIfIdle).setIdleTtl(idleTtl).setNoRebuild(noRebuild).setRePopTime(rePopTime).setNoSpawn(noSpawn);
        if (baseItemTypeEntity != null) {
            botItemConfig.setBaseItemTypeId(baseItemTypeEntity.getId());
        }
        if (place != null) {
            botItemConfig.setPlace(place.toPlaceConfig());
        }
        return botItemConfig;
    }

    public void fromBotItemConfig(ItemTypePersistence itemTypePersistence, BotItemConfig botItemConfig) {
        baseItemTypeEntity = itemTypePersistence.readBaseItemTypeEntity(botItemConfig.getBaseItemTypeId());
        count = botItemConfig.getCount();
        createDirectly = botItemConfig.isCreateDirectly();
        noSpawn = botItemConfig.isNoSpawn();
        if (botItemConfig.getPlace() != null) {
            place = new PlaceConfigEntity();
            place.fromPlaceConfig(botItemConfig.getPlace());
        } else {
            place = null;
        }
        angle = botItemConfig.getAngle();
        moveRealmIfIdle = botItemConfig.isMoveRealmIfIdle();
        idleTtl = botItemConfig.getIdleTtl();
        noRebuild = botItemConfig.isNoRebuild();
        rePopTime = botItemConfig.getRePopTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotItemConfigEntity that = (BotItemConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
