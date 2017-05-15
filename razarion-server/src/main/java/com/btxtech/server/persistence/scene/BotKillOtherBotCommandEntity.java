package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;

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
@Table(name = "SCENE_BOT_KILL_OTHER_BOT_COMMAND")
public class BotKillOtherBotCommandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BotConfigEntity botConfigEntity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BotConfigEntity targetBotConfigEntity;

    public BotKillOtherBotCommandConfig toBotKillOtherBotCommandConfig() {
        BotKillOtherBotCommandConfig botKillOtherBotCommandConfig = new BotKillOtherBotCommandConfig();
        if (botConfigEntity != null) {
            botKillOtherBotCommandConfig.setBotId(botConfigEntity.getId());
        }
        if (targetBotConfigEntity != null) {
            botKillOtherBotCommandConfig.setTargetBotId(targetBotConfigEntity.getId());
        }
        return botKillOtherBotCommandConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotKillOtherBotCommandEntity that = (BotKillOtherBotCommandEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
