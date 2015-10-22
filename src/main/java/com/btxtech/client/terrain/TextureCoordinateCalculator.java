package com.btxtech.client.terrain;

import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 21.10.2015.
 */
public class TextureCoordinateCalculator {
    private Vertex sAxis;
    private Vertex tAxis;
    private Logger logger = Logger.getLogger(TextureCoordinateCalculator.class.getName());

    public TextureCoordinateCalculator(Triangle triangle) {
        Vertex normal = triangle.calculateNorm().normalize(1.0);
        sAxis = new Vertex(0, 0, 1).cross(normal);
        tAxis = normal.cross(sAxis);
        if (sAxis.magnitude() == 0.0) {
            logger.severe("sAxis.magnitude() == 0.0");
            logger.severe("triangle: " + triangle);
            sAxis = new Vertex(1, 0, 0);
        }
        if (tAxis.magnitude() == 0.0) {
            logger.severe("tAxis.magnitude() == 0.0");
            logger.severe("triangle: " + triangle);
            tAxis = new Vertex(0, 0, 1);
        }
    }

    public TextureCoordinateCalculator(Vertex sAxis, Vertex tAxis) {
        this.sAxis = sAxis;
        this.tAxis = tAxis;
    }

    public Vertex getSAxis() {
        return sAxis;
    }

    public Vertex getTAxis() {
        return tAxis;
    }
}
