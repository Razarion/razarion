package com.btxtech.client.renderer.model;

import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Created by Beat
 * 15.09.2015.
 */
@RunWith(CdiRunner.class)
public class ShadowingTest {
    @Inject
    private Shadowing shadowing;

    @Test
    public void testCreateMvpShadowBias() throws Exception {
        Matrix4 mvpShadowBias = shadowing.createMvpShadowBias();
        Vertex vertex = mvpShadowBias.multiply(new Vertex(0, 0, 0), 1.0);
        System.out.println(vertex);
        double w = mvpShadowBias.multiplyW(new Vertex(0, 0, 0), 1.0);
        System.out.println(w);
        System.out.println(vertex.divide(w));
    }
}