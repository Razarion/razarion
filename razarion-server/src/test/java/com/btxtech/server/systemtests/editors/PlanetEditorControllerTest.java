package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.PlanetEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.rest.PlanetEditorController;
import org.junit.After;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

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
        setupItemTypes();
    }

    @After
    public void cleanTables() {
        cleanTableNative("PLANET_LIMITATION");
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
        registerUpdate(planetConfig -> planetConfig.setItemTypeLimitation(setupItemTypeLimitation1()));
        registerUpdate(planetConfig -> planetConfig.setItemTypeLimitation(setupItemTypeLimitation2()));
    }

    private Map<Integer, Integer> setupItemTypeLimitation1() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(BASE_ITEM_TYPE_BULLDOZER_ID, 33);
        map.put(BASE_ITEM_TYPE_HARVESTER_ID, 22);
        return map;
    }

    private Map<Integer, Integer> setupItemTypeLimitation2() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(BASE_ITEM_TYPE_ATTACKER_ID, 44);
        map.put(BASE_ITEM_TYPE_FACTORY_ID, 55);
        return map;
    }

    @Override
    protected void assertCreated(PlanetConfig config1) {
        assertThat(config1, allOf(
                hasProperty("size", equalTo(new DecimalPosition(640, 640)))
        ));
    }
}
