package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "BOT_CONFIG_BOT_ITEM")
public class BotItemConfigEntity extends BaseEntity {
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
        BotItemConfig botItemConfig = new BotItemConfig().count(count).createDirectly(createDirectly).angle(angle).moveRealmIfIdle(moveRealmIfIdle).idleTtl(idleTtl).noRebuild(noRebuild).rePopTime(rePopTime).noSpawn(noSpawn);
        if (baseItemTypeEntity != null) {
            botItemConfig.baseItemTypeId(baseItemTypeEntity.getId());
        }
        if (place != null) {
            botItemConfig.place(place.toPlaceConfig());
        }
        return botItemConfig;
    }

    public void fromBotItemConfig(BotItemConfig botItemConfig) {
        baseItemTypeEntity = (BaseItemTypeEntity) new BaseItemTypeEntity().id(botItemConfig.getBaseItemTypeId());
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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }

}
