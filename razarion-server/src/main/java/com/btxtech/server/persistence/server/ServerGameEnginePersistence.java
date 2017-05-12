package com.btxtech.server.persistence.server;

import com.btxtech.server.persistence.PlanetPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.dto.SlavePlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 09.05.2017.
 */
@Singleton
public class ServerGameEnginePersistence {
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private PlanetPersistence planetPersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;

    @Transactional
    public SlavePlanetConfig readSlavePlanetConfig() {
        return read().getSlavePlanetConfig();
    }

    @Transactional
    public MasterPlanetConfig readMasterPlanetConfig() {
        return read().getMasterPlanetConfig();
    }

    @Transactional
    public PlanetConfig readPlanetConfig() {
        return read().getPlanetConfig();
    }

    @Transactional
    public Collection<BotConfig> readBotConfigs() {
        return read().getBotConfigs();
    }

    @Transactional
    @SecurityCheck
    public void updatePlanetConfig(Integer planetConfigId) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = read();
        if(planetConfigId != null) {
            serverGameEngineConfigEntity.setPlanetEntity(planetPersistence.loadPlanet(planetConfigId));
        } else {
            serverGameEngineConfigEntity.setPlanetEntity(null);
        }
        entityManager.merge(serverGameEngineConfigEntity);
    }

    @Transactional
    @SecurityCheck
    public void updateStartRegion(List<DecimalPosition> startRegion) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = read();
        serverGameEngineConfigEntity.setStartRegion(startRegion);
        entityManager.merge(serverGameEngineConfigEntity);
    }

    @Transactional
    @SecurityCheck
    public void updateResourceRegionConfigs(List<ResourceRegionConfig> resourceRegionConfigs) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = read();
        serverGameEngineConfigEntity.setResourceRegionConfigs(itemTypePersistence, resourceRegionConfigs);
        entityManager.merge(serverGameEngineConfigEntity);
    }

    @Transactional
    @SecurityCheck
    public void updateBotConfigs(List<BotConfig> botConfigs) {
        ServerGameEngineConfigEntity serverGameEngineConfigEntity = read();
        serverGameEngineConfigEntity.setBotConfigs(itemTypePersistence, botConfigs);
        entityManager.merge(serverGameEngineConfigEntity);
    }

    private ServerGameEngineConfigEntity read() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ServerGameEngineConfigEntity> userQuery = criteriaBuilder.createQuery(ServerGameEngineConfigEntity.class);
        Root<ServerGameEngineConfigEntity> from = userQuery.from(ServerGameEngineConfigEntity.class);
        CriteriaQuery<ServerGameEngineConfigEntity> userSelect = userQuery.select(from);
        List<ServerGameEngineConfigEntity> serverGameEngineConfigEntities = entityManager.createQuery(userSelect).getResultList();
        if (serverGameEngineConfigEntities.isEmpty()) {
            ServerGameEngineConfigEntity serverGameEngineConfigEntity = new ServerGameEngineConfigEntity();
            entityManager.persist(serverGameEngineConfigEntity);
            return serverGameEngineConfigEntity;
        }
        if (serverGameEngineConfigEntities.size() > 1) {
            throw new IllegalStateException("More then one ServerGameEngineConfigEntity found: " + serverGameEngineConfigEntities.size());
        }
        return serverGameEngineConfigEntities.get(0);
    }
}
