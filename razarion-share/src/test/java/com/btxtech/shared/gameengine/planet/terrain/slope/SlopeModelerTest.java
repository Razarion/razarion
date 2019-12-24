package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ShapeTest;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.utils.FractalFieldGenerator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 14.01.2019.
 */
public class SlopeModelerTest {

    @Test
    public void sculpt() {
        SlopeConfig slopeConfig = new SlopeConfig();
        SlopeSkeletonConfig slopeSkeletonConfig = new SlopeSkeletonConfig();
        slopeConfig.setSlopeSkeletonConfig(slopeSkeletonConfig);
        slopeSkeletonConfig.setSegments(10);
        slopeSkeletonConfig.setSlopeShapes(ShapeTest.toSlopeShapeList(new DecimalPosition(0, 100), new DecimalPosition(0, 50), new DecimalPosition(0, 0)));

        double[][] fractalField = FractalFieldGenerator.createFractalField(slopeConfig.toFractalFiledConfig());


        SlopeModeler.sculpt(slopeSkeletonConfig, fractalField);

        Assert.fail("... VERIFY ...");
    }
}