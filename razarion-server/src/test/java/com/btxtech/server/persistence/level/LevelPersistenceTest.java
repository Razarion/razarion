package com.btxtech.server.persistence.level;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.comparator.impl.ObjectComparatorIgnore;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
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
        ImageLibraryEntity image1 = persistInTransaction(new ImageLibraryEntity());
        ImageLibraryEntity image2 = persistInTransaction(new ImageLibraryEntity());
        setupItemTypes();
        // Create
        LevelEditConfig levelEditConfig = levelPersistence.create();
        // Update
        levelEditConfig.setNumber(1);
        levelEditConfig.setXp2LevelUp(200);
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(BASE_ITEM_TYPE_ATTACKER_ID, 1);
        itemTypeLimitation.put(BASE_ITEM_TYPE_BULLDOZER_ID, 2);
        itemTypeLimitation.put(BASE_ITEM_TYPE_TOWER_ID, 3);
        levelEditConfig.setItemTypeLimitation(itemTypeLimitation);
        List<LevelUnlockConfig> levelUnlockConfigs = new ArrayList<>();
        levelUnlockConfigs.add(new LevelUnlockConfig().setInternalName("LevelUnlockConfig1").setBaseItemTypeCount(12).setBaseItemType(BASE_ITEM_TYPE_ATTACKER_ID).setThumbnail(image1.getId()).setI18nName(i18nHelper("LevelUnlockConfig1")));
        levelUnlockConfigs.add(new LevelUnlockConfig().setInternalName("LevelUnlockConfig2").setBaseItemTypeCount(1).setBaseItemType(BASE_ITEM_TYPE_TOWER_ID).setThumbnail(image2.getId()).setI18nName(i18nHelper("LevelUnlockConfig2")));
        levelEditConfig.setLevelUnlockConfigs(levelUnlockConfigs);
        levelPersistence.update(levelEditConfig);
        // Read
        List<LevelConfig> levelConfigs = levelPersistence.read();
        LevelConfig readLevelConfig = getLevelConfig(levelEditConfig.getLevelId(), levelConfigs);
        Assert.assertEquals(levelEditConfig.getLevelId(), readLevelConfig.getLevelId());
        Assert.assertEquals(1, readLevelConfig.getNumber());
        Assert.assertEquals(3, readLevelConfig.getItemTypeLimitation().size());
        Assert.assertEquals(1, (int) readLevelConfig.getItemTypeLimitation().get(BASE_ITEM_TYPE_ATTACKER_ID));
        Assert.assertEquals(2, (int) readLevelConfig.getItemTypeLimitation().get(BASE_ITEM_TYPE_BULLDOZER_ID));
        Assert.assertEquals(3, (int) readLevelConfig.getItemTypeLimitation().get(BASE_ITEM_TYPE_TOWER_ID));
        // Read 2
        LevelEntity levelEntity = levelPersistence.read(levelEditConfig.getLevelId());
        Assert.assertEquals(levelEditConfig.getLevelId(), (int) levelEntity.getId());
        LevelEditConfig actual1 = levelPersistence.readLevelConfig(levelEntity.getId());
        List<LevelUnlockConfig> actualLevelUnlockConfigs = actual1.getLevelUnlockConfigs();
        ObjectComparatorIgnore.add(LevelUnlockConfig.class, "id");
        ReflectionAssert.assertReflectionEquals(levelUnlockConfigs, actualLevelUnlockConfigs);
        ObjectComparatorIgnore.clear();
        // Modify unlocks
        actualLevelUnlockConfigs.get(0).setBaseItemType(BASE_ITEM_TYPE_BULLDOZER_ID).setCrystalCost(333).setI18nName(i18nHelper("LevelUnlockConfig1"));
        levelPersistence.update(actual1);
        LevelEditConfig actual2 = levelPersistence.readLevelConfig(levelEntity.getId());
        ReflectionAssert.assertReflectionEquals(actualLevelUnlockConfigs, actual2.getLevelUnlockConfigs());
        // Delete
        levelPersistence.delete(levelEditConfig.getLevelId());
        try {
            levelPersistence.read(levelEditConfig.getLevelId());
            Assert.fail("Expected: IllegalArgumentException(\"No Level for id: \" + id)");
        } catch (IllegalArgumentException ignore) {
            // Expected
        }
        assertEmptyCount(LevelEntity.class);
        assertEmptyCount(LevelUnlockEntity.class);
        assertEmptyCountNative("LEVEL_LIMITATION");
        cleanTable(ImageLibraryEntity.class);
        cleanItemTypes();
    }

    @Test
    public void testStarterLevelId() throws Exception {
        setupLevels();
        Assert.assertEquals(LEVEL_1_ID, (int) levelPersistence.getStarterLevel().getId());
        cleanLevels();
    }

    @Test
    public void testLevelNumber4Id() throws Exception {
        setupLevels();
        Assert.assertEquals(1, levelPersistence.getLevelNumber4Id(LEVEL_1_ID));
        Assert.assertEquals(2, levelPersistence.getLevelNumber4Id(LEVEL_2_ID));
        Assert.assertEquals(3, levelPersistence.getLevelNumber4Id(LEVEL_3_ID));
        Assert.assertEquals(4, levelPersistence.getLevelNumber4Id(LEVEL_4_ID));
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