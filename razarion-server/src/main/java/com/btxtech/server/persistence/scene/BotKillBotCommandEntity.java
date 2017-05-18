package com.btxtech.server.persistence.scene;

import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 15.05.2017.
 */
@Entity
@Table(name = "SCENE_BOT_KILL_BOT_COMMAND")
public class BotKillBotCommandEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer botAuxiliaryIdId;

    public KillBotCommandConfig toKillBotCommandConfig() {
        return new KillBotCommandConfig().setBotAuxiliaryId(botAuxiliaryIdId);
    }

    public void fromKillBotCommandConfig(KillBotCommandConfig killBotCommandConfig) {
        botAuxiliaryIdId = killBotCommandConfig.getBotAuxiliaryId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BotKillBotCommandEntity that = (BotKillBotCommandEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
