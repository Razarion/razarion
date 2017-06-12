package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Beat
 * on 11.06.2017.
 */
@RunWith(EasyMockRunner.class)
public class EdgeTest {
    @Mock
    private Slope slope;

    @Test
    public void test() {
        EasyMock.expect(slope.getSlopeSkeletonConfig()).andReturn(new SlopeSkeletonConfig().setWidth(3));
        EasyMock.replay(slope);

        Driveway.Edge edge = new Driveway.Edge(slope, new DecimalPosition(0, 0));
        edge.init(new DecimalPosition(20, 0));

     //   Assert.assertEquals(0, edge.getInterpolateDrivewayHeightFactor(new DecimalPosition(0, 0)),0.0001);
        Assert.assertEquals(0.5, edge.getInterpolateDrivewayHeightFactor(new DecimalPosition(8.5, 0)),0.0001);
        Assert.assertEquals(1, edge.getInterpolateDrivewayHeightFactor(new DecimalPosition(17, 0)),0.0001);
    }

}