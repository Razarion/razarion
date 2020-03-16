package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.rest.LevelEditorController;
import org.junit.After;
import org.junit.Before;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LevelEditorControllerTest extends AbstractCrudTest<LevelEditorController, LevelConfig> {
    public LevelEditorControllerTest() {
        super(LevelEditorController.class, LevelConfig.class);
    }

    @Before
    public void fillTables() {
        setupItemTypes();
    }

    @After
    public void cleanTables() {
        cleanTableNative("LEVEL_LIMITATION");
        cleanTable(LevelEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(levelConfig -> levelConfig.number(1).xp2LevelUp(2));
        registerUpdate(levelConfig -> levelConfig.number(4).xp2LevelUp(1));
        Map<Integer, Integer> itm1 = new HashMap<>();
        itm1.put(BASE_ITEM_TYPE_BULLDOZER_ID, 2);
        itm1.put(BASE_ITEM_TYPE_HARVESTER_ID, 1);
        registerUpdate(levelConfig -> levelConfig.setItemTypeLimitation(itm1));
        Map<Integer, Integer> itm2 = new HashMap<>();
        itm2.put(BASE_ITEM_TYPE_HARVESTER_ID, 5);
        registerUpdate(levelConfig -> levelConfig.setItemTypeLimitation(itm2));
        registerUpdate(levelConfig -> levelConfig.setItemTypeLimitation(Collections.emptyMap()));
    }

}
