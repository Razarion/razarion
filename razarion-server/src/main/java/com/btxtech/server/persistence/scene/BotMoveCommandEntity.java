package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BotMoveCommandConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
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
 * 15.05.2017.
 */
@Entity
@Table(name = "SCENE_BOT_MOVE_COMMAND")
public class BotMoveCommandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BotConfigEntity botConfigEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity baseItemType;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "targetPositionX")),
            @AttributeOverride(name = "y", column = @Column(name = "targetPositionY")),
    })
    private DecimalPosition targetPosition;

    public BotMoveCommandConfig toBotMoveCommandConfig() {
        BotMoveCommandConfig botMoveCommandConfig = new BotMoveCommandConfig();
        if (botConfigEntity != null) {
            botMoveCommandConfig.setBotId(botConfigEntity.getId());
        }
        if (baseItemType != null) {
            botMoveCommandConfig.setBaseItemTypeId(baseItemType.getId());
        }
        return botMoveCommandConfig.setTargetPosition(targetPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotMoveCommandEntity that = (BotMoveCommandEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
