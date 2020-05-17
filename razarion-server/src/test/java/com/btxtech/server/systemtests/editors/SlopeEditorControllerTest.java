package com.btxtech.server.systemtests.editors;

import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.surface.SlopeShapeEntity;
import com.btxtech.server.systemtests.framework.AbstractCrudTest;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.rest.SlopeEditorController;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;

public class SlopeEditorControllerTest extends AbstractCrudTest<SlopeEditorController, SlopeConfig> {
    public SlopeEditorControllerTest() {
        super(SlopeEditorController.class, SlopeConfig.class);
    }

    @Before
    public void fillTables() {
        setupImages();
        setupGroundConfig();
    }

    @After
    public void cleanTables() {
        cleanTable(SlopeShapeEntity.class);
        cleanTable(SlopeConfigEntity.class);
    }

    @Override
    protected void setupUpdate() {
        registerUpdate(groundConfig -> groundConfig.coastDelimiterLineGameEngine(10).outerLineGameEngine(5).innerLineGameEngine(20).horizontalSpace(5));
        registerUpdate(groundConfig -> groundConfig.coastDelimiterLineGameEngine(20).outerLineGameEngine(27).innerLineGameEngine(50).horizontalSpace(25).groundConfigId(GROUND_1_ID));
        registerUpdate(groundConfig -> groundConfig.groundConfigId(null));
        registerUpdate(groundConfig -> groundConfig.slopeShapes(Arrays.asList(
                new SlopeShape().slopeFactor(0.5),
                new SlopeShape().position(new DecimalPosition(1.1, 2.3)).slopeFactor(0.5),
                new SlopeShape().position(new DecimalPosition(3.4, 4.5)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(5.7, 8.1)).slopeFactor(0.4))));
        registerUpdate(groundConfig -> groundConfig.slopeShapes(Arrays.asList(
                new SlopeShape().slopeFactor(0.4),
                new SlopeShape().position(new DecimalPosition(2.1, 5.3)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(7.7, 18.1)).slopeFactor(0.0))));
        registerUpdate(groundConfig -> groundConfig.slopeShapes(null));
    }
}
