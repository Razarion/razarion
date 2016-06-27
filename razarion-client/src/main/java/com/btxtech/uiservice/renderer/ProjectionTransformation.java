package com.btxtech.uiservice.renderer;

import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 26.09.2015.
 * <p/>
 * http://www.songho.ca/opengl/gl_projectionmatrix.html
 */
@Singleton
public class ProjectionTransformation {
    private static final int Z_NEAR_FALLBACK = 10;
    private static final int Z_FAR_FALLBACK = 5000000;
    // private Logger logger = Logger.getLogger(ProjectionTransformation.class.getName());
    private double fovY;
    private double aspectRatio;
    @Inject
    private Camera camera;
    @Inject
    private TerrainSurface terrainSurface;

    public ProjectionTransformation() {
        setFovY(Math.toRadians(45));
    }

    public double getFovY() {
        return fovY;
    }

    public void setFovY(double fovY) {
        this.fovY = fovY;
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Matrix4 createMatrix() {
        return makePerspectiveFrustum(fovY, aspectRatio, calculateZNear(), calculateZFar());
    }

    public double calculateZFar() {
        Vertex cameraPosition = camera.getPosition();
        Vertex zFarPosition = calculateViewField(terrainSurface.getLowestPointInView()).calculateLongestLegZ(cameraPosition);
        if (zFarPosition == null) {
            return Z_FAR_FALLBACK;
        }

        Vertex cameraDirection = camera.getDirection();
        return cameraDirection.projection(zFarPosition.sub(cameraPosition));
    }

    public double calculateZNear() {
        Vertex cameraPosition = camera.getPosition();
        Vertex zNearPosition = calculateViewField(terrainSurface.getHighestPointInView()).calculateShortestLegZ(cameraPosition);
        if (zNearPosition == null) {
            return Z_NEAR_FALLBACK;
        }

        Vertex cameraDirection = camera.getDirection();
        double zNear = cameraDirection.projection(zNearPosition.sub(cameraPosition));
        if (zNear < Z_NEAR_FALLBACK) {
            return Z_NEAR_FALLBACK;
        } else {
            return zNear;
        }
    }

    /**
     * Calculates the perspective frustum projection matrix
     *
     * @param fovY        field of view y in radians
     * @param aspectRatio aspect ratio width / height
     * @param zNear       z near
     * @param zFar        z far
     * @return perspective frustum projection matrix
     */
    public static Matrix4 makePerspectiveFrustum(double fovY, double aspectRatio, double zNear, double zFar) {
        double top = zNear * Math.tan(fovY / 2.0);
        double right = top * aspectRatio;

        return makeBalancedPerspectiveFrustum(right, top, zNear, zFar);
    }

    public static Matrix4 makeBalancedPerspectiveFrustum(double right, double top, double zNear, double zFar) {
        double x = zNear / right;
        double y = zNear / top;
        double a = -(zFar + zNear) / (zFar - zNear);
        double b = -2 * zFar * zNear / (zFar - zNear);

        return new Matrix4(new double[][]{
                {x, 0, 0, 0},
                {0, y, 0, 0},
                {0, 0, a, b},
                {0, 0, -1, 0}});
    }

    /**
     * Calculates a polygon representing the view field for the given z.
     * <p/>
     *
     * @param z ground level
     * @return camera View field view
     */
    public ViewField calculateViewField(double z) {
        double y = 1.0 / (Math.tan(MathHelper.QUARTER_RADIANT - fovY / 2.0));
        double x = y * aspectRatio;

        Matrix4 cameraRotationMatrix = Matrix4.createZRotation(camera.getRotateZ()).multiply(Matrix4.createXRotation(camera.getRotateX()));
        Vertex cameraPosition = camera.getPosition();

        boolean topValid = MathHelper.isInSection(camera.getRotateX() + fovY / 2.0, -MathHelper.QUARTER_RADIANT, MathHelper.HALF_RADIANT);
        boolean bottomValid = MathHelper.isInSection(camera.getRotateX() - fovY / 2.0, -MathHelper.QUARTER_RADIANT, MathHelper.HALF_RADIANT);

        ViewField viewField = new ViewField(z);

        if (bottomValid) {
            Vertex cameraBottomLeftDirection = new Vertex(-x, -y, -1);
            cameraBottomLeftDirection = cameraRotationMatrix.multiply(cameraBottomLeftDirection, 1.0);
            double m = (z - camera.getTranslateZ()) / cameraBottomLeftDirection.getZ();
            viewField.setBottomLeft(cameraPosition.add(cameraBottomLeftDirection.multiply(m)).toXY());

            Vertex cameraBottomRightDirection = new Vertex(x, -y, -1);
            cameraBottomRightDirection = cameraRotationMatrix.multiply(cameraBottomRightDirection, 1.0);
            m = (z - camera.getTranslateZ()) / cameraBottomRightDirection.getZ();
            viewField.setBottomRight(cameraPosition.add(cameraBottomRightDirection.multiply(m)).toXY());
        }

        if (topValid) {
            Vertex cameraTopRightDirection = new Vertex(x, y, -1);
            cameraTopRightDirection = cameraRotationMatrix.multiply(cameraTopRightDirection, 1.0);
            double m = (z - camera.getTranslateZ()) / cameraTopRightDirection.getZ();
            viewField.setTopRight(cameraPosition.add(cameraTopRightDirection.multiply(m)).toXY());

            Vertex cameraTopLefDirection = new Vertex(-x, y, -1);
            cameraTopLefDirection = cameraRotationMatrix.multiply(cameraTopLefDirection, 1.0);
            m = (z - camera.getTranslateZ()) / cameraTopLefDirection.getZ();
            viewField.setTopLeft(cameraPosition.add(cameraTopLefDirection.multiply(m)).toXY());
        }


        return viewField;
    }

    /**
     * Creates the pick ray for converting the mouse position to the model position
     *
     * @param clip clip coordinates (-1 to 1)
     */
    public Ray3d createPickRay(DecimalPosition clip) {
        double zNear = calculateZNear();
        double top = zNear * Math.tan(fovY / 2.0);
        double y = top * clip.getY();
        double x = clip.getX() * top * aspectRatio;
        double rotateY = -Math.atan(x / zNear);
        double rotateX = Math.atan(y / zNear);
        Vertex direction = new Vertex(0, 0, -1);
        Matrix4 rotation = Matrix4.createXRotation(rotateX).multiply(Matrix4.createYRotation(rotateY));
        direction = rotation.multiply(direction, 1.0);
        return new Ray3d(new Vertex(0, 0, 0), direction);
    }

    @Override
    public String toString() {
        return "ProjectionTransformation{" +
                "fovY=" + Math.toDegrees(fovY) +
                ", aspectRatio=" + aspectRatio +
                '}';
    }
}
