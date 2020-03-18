package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.GameUiControlContextEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.dto.GameUiContextConfig;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.rest.GameUiContextEditorController;
import org.junit.After;
import org.junit.Before;

public class GameUiContextEditorControllerTest extends AbstractCrudTest<GameUiContextEditorController, GameUiContextConfig> {
    public GameUiContextEditorControllerTest() {
        super(GameUiContextEditorController.class, GameUiContextConfig.class);
    }

    @Before
    public void fillTables() {
        setupPlanets();
        setupLevels();
    }

    @After
    public void cleanTables() {
        cleanTable(GameUiControlContextEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(gameUiContextConfig -> gameUiContextConfig.gameEngineMode(GameEngineMode.MASTER));
        registerUpdate(gameUiContextConfig -> gameUiContextConfig.gameEngineMode(GameEngineMode.SLAVE));
        registerUpdate(gameUiContextConfig -> gameUiContextConfig.planetId(PLANET_1_ID).minimalLevel(LEVEL_1_ID));
        registerUpdate(gameUiContextConfig -> gameUiContextConfig.planetId(PLANET_2_ID).minimalLevel(LEVEL_2_ID));
        registerUpdate(gameUiContextConfig -> gameUiContextConfig.planetId(null));
        registerUpdate(gameUiContextConfig -> gameUiContextConfig.minimalLevel(null));
    }

}
