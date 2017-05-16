package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;

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
@Table(name = "SCENE_BOT_REMOVE_OWN_ITEMS_COMMAND")
public class BotRemoveOwnItemCommandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BotConfigEntity botConfigEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BaseItemTypeEntity baseItemType2Remove;

    public BotRemoveOwnItemCommandConfig toBotRemoveOwnItemCommandConfig() {
        BotRemoveOwnItemCommandConfig botRemoveOwnItemCommandConfig = new BotRemoveOwnItemCommandConfig();
        if (botConfigEntity != null) {
            botRemoveOwnItemCommandConfig.setBotId(botConfigEntity.getId());
        }
        if (baseItemType2Remove != null) {
            botRemoveOwnItemCommandConfig.setBaseItemType2RemoveId(baseItemType2Remove.getId());
        }
        return botRemoveOwnItemCommandConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotRemoveOwnItemCommandEntity that = (BotRemoveOwnItemCommandEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
