package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ShapeTest;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig_OLD;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 14.01.2019.
 */
public class SlopeModelerTest {

    @Test
    public void sculpt() {
        SlopeConfig_OLD slopeConfigOLD = new SlopeConfig_OLD();
        SlopeConfig slopeConfig = new SlopeConfig();
        slopeConfigOLD.setSlopeConfig(slopeConfig);
        slopeConfig.setSlopeShapes(ShapeTest.toSlopeShapeList(new DecimalPosition(0, 100), new DecimalPosition(0, 50), new DecimalPosition(0, 0)));

        // TODO double[][] fractalField = FractalFieldGenerator.createFractalField(slopeConfigOLD.toFractalFiledConfig());
        SlopeModeler.sculpt(slopeConfig);



        Assert.fail("... VERIFY ...");
    }
}