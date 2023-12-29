package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.bot.BotConfigEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by Beat
 * 15.08.2015.
 */
@ApplicationScoped
public class BotConfigEntityPersistence {
    @PersistenceContext
    private EntityManager entityManager;

    // This should not be here. But there is no BotPersistence...
    public BotConfigEntity readBotConfigEntity(Integer id) {
        if (id == null) {
            return null;
        }
        BotConfigEntity botConfigEntity = entityManager.find(BotConfigEntity.class, id);
        if (botConfigEntity == null) {
            throw new IllegalArgumentException("No BotConfigEntity for id: " + id);
        }
        return botConfigEntity;
    }
}
