package com.btxtech.client.terrain;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.client.renderer.model.MeshGroup;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 17.10.2015.
 */
public class Plateau {
    private static final int PLANE_TOP_HEIGHT = 80;
    private static final int FRACTIONAL_DISTANCE = 80;
    private static final int SLOPE_WIDTH = 4;
    private static final Rectangle INDEX_RECT = new Rectangle(30, 20, 30, 15);
    private Mesh mesh;
    private Logger logger = Logger.getLogger(Plateau.class.getName());

    public Plateau(Mesh mesh) {
        this.mesh = mesh;
    }

    public void sculpt() {
        final FractalField fractalField = FractalField.createSaveFractalField(INDEX_RECT.getWidth() + 2 * SLOPE_WIDTH, INDEX_RECT.getHeight() + 2 * SLOPE_WIDTH, FRACTIONAL_DISTANCE, -FRACTIONAL_DISTANCE, 1.0);

        // Mark top vertices
        mesh.iterate(new Mesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (INDEX_RECT.contains2(new DecimalPosition(index))) {
                    mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT));
                } else {
                    double distance = INDEX_RECT.getNearestPointInclusive(new DecimalPosition(index)).getDistance(new DecimalPosition(index));
                    if (distance < SLOPE_WIDTH) {
                        double height = PLANE_TOP_HEIGHT - PLANE_TOP_HEIGHT * distance / (double) SLOPE_WIDTH;
                        // height += fractalField.get(index);
                        // height = Math.max(Math.min(PLANE_TOP_HEIGHT, height), 0);

                        mesh.getVertexDataSafe(index).addZValue(height);
                    }
                }
            }
        });

    }

}
