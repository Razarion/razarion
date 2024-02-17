package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.gameengine.datatypes.config.LevelEditConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.rest.LevelEditorController;
import com.btxtech.test.JsonAssert;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LevelEditorControllerTest extends AbstractCrudTest<LevelEditorController, LevelEditConfig> {
    public LevelEditorControllerTest() {
        super(LevelEditorController.class, LevelEditConfig.class);
        enabledIgnoreInternalName();
    }

    @Before
    public void fillTables() {
        setupImages();
        setupItemTypes();
    }

    @After
    public void cleanTables() {
        cleanTableNative("LEVEL_LIMITATION");
        cleanTable(LevelUnlockEntity.class);
        cleanTable(LevelEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(levelConfig -> levelConfig.number(1).xp2LevelUp(2), new JsonAssert.IdSuppressor("", "internalName"));
        registerUpdate(levelConfig -> levelConfig.number(4).xp2LevelUp(1), new JsonAssert.IdSuppressor("", "internalName"));
        Map<Integer, Integer> itm1 = new HashMap<>();
        itm1.put(BASE_ITEM_TYPE_BULLDOZER_ID, 2);
        itm1.put(BASE_ITEM_TYPE_HARVESTER_ID, 1);
        registerUpdate(levelConfig -> levelConfig.setItemTypeLimitation(itm1));
        Map<Integer, Integer> itm2 = new HashMap<>();
        itm2.put(BASE_ITEM_TYPE_HARVESTER_ID, 5);
        registerUpdate(levelConfig -> levelConfig.setItemTypeLimitation(itm2));
        registerUpdate(levelConfig -> levelConfig.setItemTypeLimitation(Collections.emptyMap()));
        registerUpdate(levelConfig -> levelConfig.levelUnlockConfigs(Collections.singletonList(
                new LevelUnlockConfig()
                        .baseItemType(BASE_ITEM_TYPE_BULLDOZER_ID)
                        .baseItemTypeCount(10)
                        .thumbnail(IMAGE_1_ID)
                        .crystalCost(99))), new JsonAssert.IdSuppressor("/levelUnlockConfigs", "id", true));
        registerUpdate(levelConfig -> levelConfig.levelUnlockConfigs(Arrays.asList(
                        new LevelUnlockConfig()
                                .baseItemType(BASE_ITEM_TYPE_HARVESTER_ID)
                                .baseItemTypeCount(11)
                                .thumbnail(IMAGE_1_ID)
                                .crystalCost(9),
                        new LevelUnlockConfig()
                                .baseItemType(BASE_ITEM_TYPE_ATTACKER_ID)
                                .baseItemTypeCount(12)
                                .thumbnail(IMAGE_1_ID)
                                .crystalCost(19))),
                new JsonAssert.IdSuppressor("/levelUnlockConfigs", "id", true));
        registerUpdate(levelConfig -> levelConfig.levelUnlockConfigs(Collections.emptyList()));
    }
}
