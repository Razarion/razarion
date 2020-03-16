package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.rest.LevelEditorController;
import org.junit.After;

public class LevelEditorControllerTest extends AbstractCrudTest<LevelEditorController, LevelConfig> {
    public LevelEditorControllerTest() {
        super(LevelEditorController.class, LevelConfig.class);
    }

    @After
    public void cleanTables() {
        cleanTable(LevelEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(levelConfig -> levelConfig.number(1).xp2LevelUp(2));
        registerUpdate(levelConfig -> levelConfig.number(4).xp2LevelUp(1));
    }

}
