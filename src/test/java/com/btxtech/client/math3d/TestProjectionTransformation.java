package com.btxtech.client.math3d;

import com.btxtech.client.renderer.model.AbstractProjectionTransformation;
import com.btxtech.client.renderer.model.NormalProjectionTransformation;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.shared.primitives.Matrix4;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 13.04.2015.
 */
public class TestProjectionTransformation {

    @Test
    public void makePerspective() {
        Matrix4 actual = AbstractProjectionTransformation.makePerspective(Math.toRadians(45), 480.0 / 480.0, 0.1, 100.0);
        Matrix4 expected = new Matrix4(new double[][]{
                {2.4142135623730954, 0.0, 0.0, 0.0},
                {0.0, 2.4142135623730954, 0.0, 0.0},
                {0.0, 0.0, -1.002002002002002, -0.20020020020020018},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, actual);
        actual = AbstractProjectionTransformation.makePerspective(Math.toRadians(90), 640.0 / 640.0, 0.1, 100.0);
        expected = new Matrix4(new double[][]{
                {1.0000000000000002, 0.0, 0.0, 0.0},
                {0.0, 1.0000000000000002, 0.0, 0.0},
                {0.0, 0.0, -1.002002002002002, -0.20020020020020018},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void makeBalancedFrustumToGetRight() {
        Matrix4 actual = AbstractProjectionTransformation.makeBalancedFrustum(12, 56, 0.1, 100);
        Matrix4 expected = new Matrix4(new double[][]{
                {0.008333333333333333, 0.0, 0.0, 0.0},
                {0.0, 0.0017857142857142859, 0.0, 0.0},
                {0.0, 0.0, -1.002002002002002, -0.20020020020020018},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, actual);
    }

    // @Test
    public void createDefault() {
        ProjectionTransformation projectionTransformation = new NormalProjectionTransformation();
        Matrix4 expected = new Matrix4(new double[][]{
                {1.8106601717798214, 0.0, 0.0, 0.0},
                {0.0, 2.4142135623730954, 0.0, 0.0},
                {0.0, 0.0, -1.001000500250125, -0.2001000500250125},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, projectionTransformation.createMatrix());

    }


    @Test
    public void setterGetters() {
        ProjectionTransformation projectionTransformation = new NormalProjectionTransformation();
        projectionTransformation.setZNear(-0.1);
        projectionTransformation.setZFar(-100);
        projectionTransformation.setAspectRatio(3.0 / 4.0);
        projectionTransformation.setFovY(Math.toRadians(33));
        Assert.assertEquals(-0.1, projectionTransformation.getZNear(), 0.0001);
        Assert.assertEquals(-100, projectionTransformation.getZFar(), 0.0001);
        Assert.assertEquals(3.0 / 4.0, projectionTransformation.getAspectRatio(), 0.0001);
        Assert.assertEquals(Math.toRadians(33), projectionTransformation.getFovY(), 0.0001);
        Matrix4 expected = new Matrix4(new double[][]{
                {4.501257896788329, 0.0, 0.0, 0.0},
                {0.0, 3.3759434225912464, 0.0, 0.0},
                {0.0, 0.0, -1.002002002002002, 0.20020020020020018},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, projectionTransformation.createMatrix());

    }


}
