package com.btxtech.client.terrain;

import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.terrain.slope.Plateau;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 05.03.2016.
 */
public class GroundSlopeConnector {
    private final GroundMesh groundMesh;
    private final Plateau plateau;
    private Collection<Vertex> stampedOut = new ArrayList<>();

    public GroundSlopeConnector(GroundMesh groundMesh, Plateau plateau) {
        this.groundMesh = groundMesh;
        this.plateau = plateau;
    }

    public void stampOut() {
        groundMesh.iterate(new GroundMesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (plateau.isInside(vertex)) {
                    stampedOut.add(vertex);
                    // groundMesh.remove(index);
                }
            }
        });
    }

    public Collection<Vertex> getStampedOut() {
        return stampedOut;
    }
}
