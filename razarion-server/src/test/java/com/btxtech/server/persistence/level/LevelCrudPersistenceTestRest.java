package com.btxtech.server.persistence.level;

import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by Beat
 * 05.05.2017.
 */
public class LevelCrudPersistenceTestRest extends IgnoreOldArquillianTest {
    @Inject
    private LevelCrudPersistence levelCrudPersistence;
    @PersistenceContext
    private EntityManager entityManager;


    @Test
    public void testStarterLevelId() throws Exception {
        setupLevelDb();
        Assert.assertEquals(LEVEL_1_ID, (int) levelCrudPersistence.getStarterLevel().getId());
        // TODO cleanLevels();
    }

    @Test
    public void testLevelNumber4Id() throws Exception {
        setupLevelDb();
        Assert.assertEquals(1, levelCrudPersistence.getLevelNumber4Id(LEVEL_1_ID));
        Assert.assertEquals(2, levelCrudPersistence.getLevelNumber4Id(LEVEL_2_ID));
        Assert.assertEquals(3, levelCrudPersistence.getLevelNumber4Id(LEVEL_3_ID));
        Assert.assertEquals(4, levelCrudPersistence.getLevelNumber4Id(LEVEL_4_ID));
        // TODO cleanLevels();
    }

    private LevelConfig getLevelConfig(int levelId, List<LevelConfig> levelConfigs) {
        for (LevelConfig levelConfig : levelConfigs) {
            if (levelConfig.getId() == levelId) {
                return levelConfig;
            }
        }
        throw new IllegalArgumentException("No LevelConfig for levelId: " + levelId);
    }

}