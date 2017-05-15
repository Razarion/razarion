package com.btxtech.server.persistence.scene;

import com.btxtech.server.persistence.bot.BotConfigEntity;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;

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
@Table(name = "SCENE_BOT_KILL_HUMAN_COMMAND")
public class BotKillHumanCommandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BotConfigEntity botConfigEntity;

    public BotKillHumanCommandConfig toBotKillHumanCommandConfig() {
        BotKillHumanCommandConfig botKillHumanCommandConfig = new BotKillHumanCommandConfig();
        if (botConfigEntity != null) {
            botKillHumanCommandConfig.setBotId(botConfigEntity.getId());
        }
        return botKillHumanCommandConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotKillHumanCommandEntity that = (BotKillHumanCommandEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
