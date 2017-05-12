package com.btxtech.server.persistence.level;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 05.05.2017.
 */
public class LevelPersistenceTest extends ArquillianBaseTest {
    @Inject
    private LevelPersistence levelPersistence;
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testCrud() throws Exception {
        setupItemTypes();
        // Create
        LevelConfig levelConfig = levelPersistence.create();
        // Update
        levelConfig.setNumber(1);
        levelConfig.setXp2LevelUp(200);
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(BASE_ITEM_TYPE_ATTACKER_ID, 1);
        itemTypeLimitation.put(BASE_ITEM_TYPE_BULLDOZER_ID, 2);
        itemTypeLimitation.put(BASE_ITEM_TYPE_TOWER_ID, 3);
        levelConfig.setItemTypeLimitation(itemTypeLimitation);
        levelPersistence.update(levelConfig);
        // Read
        List<LevelConfig> levelConfigs = levelPersistence.read();
        LevelConfig readLevelConfig = getLevelConfig(levelConfig.getLevelId(), levelConfigs);
        Assert.assertEquals(levelConfig.getLevelId(), readLevelConfig.getLevelId());
        Assert.assertEquals(1, readLevelConfig.getNumber());
        Assert.assertEquals(3, readLevelConfig.getItemTypeLimitation().size());
        Assert.assertEquals(1, (int) readLevelConfig.getItemTypeLimitation().get(BASE_ITEM_TYPE_ATTACKER_ID));
        Assert.assertEquals(2, (int) readLevelConfig.getItemTypeLimitation().get(BASE_ITEM_TYPE_BULLDOZER_ID));
        Assert.assertEquals(3, (int) readLevelConfig.getItemTypeLimitation().get(BASE_ITEM_TYPE_TOWER_ID));
        // Read 2
        LevelEntity levelEntity = levelPersistence.read(levelConfig.getLevelId());
        Assert.assertEquals(levelConfig.getLevelId(), (int) levelEntity.getId());
        // Delete
        levelPersistence.delete(levelConfig.getLevelId());
        try {
            levelPersistence.read(levelConfig.getLevelId());
            Assert.fail("Expected: IllegalArgumentException(\"No Level for id: \" + id)");
        } catch (IllegalArgumentException ignore) {
            // Expected
        }
        Assert.assertEquals(0, ((Number) entityManager.createQuery("SELECT COUNT(l) FROM LevelEntity l").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM LEVEL_LIMITATION").getSingleResult()).intValue());
        cleanItemTypes();
    }

    @Test
    public void testStarterLevelId() throws Exception {
        setupLevels();
        Assert.assertEquals(LEVEL_1_ID, levelPersistence.getStarterLevelId());
        cleanLevels();
    }

    private LevelConfig getLevelConfig(int levelId, List<LevelConfig> levelConfigs) {
        for (LevelConfig levelConfig : levelConfigs) {
            if (levelConfig.getLevelId() == levelId) {
                return levelConfig;
            }
        }
        throw new IllegalArgumentException("No LevelConfig for levelId: " + levelId);
    }

}