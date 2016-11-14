package com.btxtech.client.renderer.model;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.ShadowUiService;
import org.junit.Ignore;

import javax.inject.Inject;

/**
 * Created by Beat
 * 15.09.2015.
 */
// @RunWith(CdiRunner.class)
@Ignore
public class ShadowingTest {
    @Inject
    private ShadowUiService shadowUiService;

    // @Test errai and vetoed problem
    public void testCreateMvpShadowBias() throws Exception {
        Matrix4 mvpShadowBias = shadowUiService.getShadowLookupTransformation();
        Vertex vertex = mvpShadowBias.multiply(new Vertex(0, 0, 0), 1.0);
        System.out.println(vertex);
        double w = mvpShadowBias.multiplyW(new Vertex(0, 0, 0), 1.0);
        System.out.println(w);
        System.out.println(vertex.divide(w));
    }
}