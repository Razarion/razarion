package com.btxtech.server.systemtests;

import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.rest.PlanetEditorController;
import org.junit.After;
import org.junit.Before;

public class PlanetEditorControllerTest extends AbstractCrudTest<PlanetEditorController, PlanetConfig> {
    public PlanetEditorControllerTest() {
        super(PlanetEditorController.class, PlanetConfig.class);
    }

    @Before
    public void fillTable() {
        setupGroundConfig();
    }

    @After
    public void cleanTables() {
        cleanTable(PlanetEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(planetConfig -> planetConfig.houseSpace(10));
        registerUpdate(planetConfig -> planetConfig.playGround(new Rectangle2D(1.1, 2.2, 3.3, 4.4)));
        registerUpdate(planetConfig -> planetConfig.startRazarion(99));
        registerUpdate(planetConfig -> planetConfig.terrainTileDimension(new Rectangle(1, 2, 3, 4)));
        registerUpdate(planetConfig -> planetConfig.setGroundConfigId(GROUND_1_ID));
        registerUpdate(planetConfig -> planetConfig.setGroundConfigId(GROUND_2_ID));
        registerUpdate(planetConfig -> planetConfig.setGroundConfigId(null));
    }
}
