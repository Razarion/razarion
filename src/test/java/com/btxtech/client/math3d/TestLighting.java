package com.btxtech.client.math3d;

import com.btxtech.game.jsre.common.MathHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 24.06.2015.
 */
public class TestLighting {

    @Test
    public void directional() {
        Lighting lighting = new Lighting();
        lighting.setAzimuth(0);
        // Change azimuth
        lighting.setAltitude(0);
        Assert.assertEquals(new Vertex(1.0, 0, 0), lighting.getLightDirection());
        lighting.setAzimuth(MathHelper.QUARTER_RADIANT);
        Assert.assertEquals(new Vertex(6.123233995736766E-17, 1.0, 0), lighting.getLightDirection());
        lighting.setAzimuth(MathHelper.HALF_RADIANT);
        Assert.assertEquals(new Vertex(-1.0, 1.2246467991473532E-16, 0), lighting.getLightDirection());
        lighting.setAzimuth(MathHelper.THREE_QUARTER_RADIANT);
        Assert.assertEquals(new Vertex(-1.8369701987210297E-16, -1.0, 0), lighting.getLightDirection());
        // Change altitude
        lighting.setAzimuth(0);
        lighting.setAltitude(MathHelper.QUARTER_RADIANT);
        Assert.assertEquals(new Vertex(6.123233995736766E-17, 0, 1.0), lighting.getLightDirection());
        lighting.setAltitude(MathHelper.QUARTER_RADIANT / 2);
        Assert.assertEquals(new Vertex(0.7071067811865476, 0, 0.7071067811865475), lighting.getLightDirection());
    }
}
