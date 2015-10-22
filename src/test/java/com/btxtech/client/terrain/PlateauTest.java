package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.client.renderer.model.MeshGroup;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Beat
 * 20.10.2015.
 */
public class PlateauTest {

    @Test
    public void testProvideVertexListSlope() throws Exception {
        Mesh mesh = new Mesh();
        mesh.fill(1024, 1024, 32);

        MeshGroup planeMeshGroup = mesh.createMeshGroup();

        Plateau plateau = new Plateau(mesh);
        plateau.assignVertices(planeMeshGroup);

        plateau.provideVertexListSlope(new ImageDescriptor("chess08.jpg", 512, 512));
    }
}