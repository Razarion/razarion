package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.rest.PlanetEditorController;
import org.junit.After;
import org.junit.Before;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

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
        registerUpdate(planetConfig -> planetConfig.setSize(new DecimalPosition(960, 960)));
        registerUpdate(planetConfig -> planetConfig.setSize(new DecimalPosition(320, 320)));
        registerUpdate(planetConfig -> planetConfig.startRazarion(99));
        registerUpdate(planetConfig -> planetConfig.setGroundConfigId(GROUND_1_ID));
        registerUpdate(planetConfig -> planetConfig.setGroundConfigId(GROUND_2_ID));
        registerUpdate(planetConfig -> planetConfig.setGroundConfigId(null));
    }

    @Override
    protected void assertCreated(PlanetConfig config1) {
        assertThat(config1, allOf(
                hasProperty("size", equalTo(new DecimalPosition(640, 640)))
        ));
    }
}
