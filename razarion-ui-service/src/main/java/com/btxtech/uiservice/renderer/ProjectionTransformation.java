package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 26.09.2015.
 * <p>
 * http://www.songho.ca/opengl/gl_projectionmatrix.html
 */
@ApplicationScoped
public class ProjectionTransformation {
    private static final int Z_NEAR_FALLBACK = 10;
    private static final int Z_FAR_FALLBACK = 5000000;
    private static final double MIN_FOV_Y = Math.toRadians(30);
    private static final double MAX_FOV_Y = Math.toRadians(70);
    private static final double DEFAULT_FOV_Y = Math.toRadians(45);
    // private Logger logger = Logger.getLogger(ProjectionTransformation.class.getName());
    @Inject
    private Camera camera;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ShadowUiService shadowUiService;
    private double fovY = DEFAULT_FOV_Y;
    private double aspectRatio = 4.0 / 3.0;
    private Matrix4 matrix;
    private double zNear;
    private double zFar;
    private boolean fovYConstrain = true;
    private boolean disableFovYChange;

    public double getFovY() {
        return fovY;
    }

    public void setFovY(double fovY) {
        this.fovY = fovY;
        setupMatrices();
    }

    public void setDefaultFovY() {
        setFovY(DEFAULT_FOV_Y);
    }

    public void setConstrainedFovY(double fovY) {
        if (disableFovYChange) {
            return;
        }
        if (fovYConstrain) {
            setFovY(MathHelper.clamp(fovY, MIN_FOV_Y, MAX_FOV_Y));
        } else {
            setFovY(fovY);
        }
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
        setupMatrices();
    }

    public Matrix4 getMatrix() {
        return matrix;
    }

    public double getZFar() {
        return zFar;
    }

    public double getZNear() {
        return zNear;
    }

    public void setupMatrices() {
        matrix = makePerspectiveFrustum(fovY, aspectRatio, getZNear(), getZFar());
        calculateZFar();
        calculateZNear();
        shadowUiService.setupMatrices();
    }

    private void calculateZFar() {
        Vertex cameraPosition = camera.getPosition();
        Vertex zFarPosition = calculateViewField(terrainUiService.getLowestPointInView()).calculateLongestLegZ(cameraPosition);
        if (zFarPosition == null) {
            zFar = Z_FAR_FALLBACK;
            return;
        }

        Vertex cameraDirection = camera.getDirection();
        zFar = cameraDirection.projection(zFarPosition.sub(cameraPosition));
    }

    private void calculateZNear() {
        Vertex cameraPosition = camera.getPosition();
        Vertex zNearPosition = calculateViewField(terrainUiService.getHighestPointInView()).calculateShortestLegZ(cameraPosition);
        if (zNearPosition == null) {
            zNear = Z_NEAR_FALLBACK;
            return;
        }

        Vertex cameraDirection = camera.getDirection();
        double zNear = cameraDirection.projection(zNearPosition.sub(cameraPosition));
        if (zNear < Z_NEAR_FALLBACK) {
            this.zNear = Z_NEAR_FALLBACK;
        } else {
            this.zNear = zNear;
        }
    }

    /**
     * Calculates the view field for the given z.
     * <p>
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
        double zNear = getZNear();
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

    public DecimalPosition viewFieldCenterToCamera(DecimalPosition position, double z) {
        double topXRotDistance = Math.tan(camera.getRotateX() + fovY / 2.0) * (camera.getTranslateZ() - z);
        double bottomXRotDistance = Math.tan(camera.getRotateX() - fovY / 2.0) * (camera.getTranslateZ() - z);
        double xRotDistance = (topXRotDistance + bottomXRotDistance) / 2.0;
        return position.getPointWithDistance(camera.getRotateZ() + MathHelper.THREE_QUARTER_RADIANT, xRotDistance);
    }

    public void disableFovYConstrain() {
        fovYConstrain = false;
    }

    public void setDisableFovYChange(boolean disableFovYChange) {
        this.disableFovYChange = disableFovYChange;
    }

    @Override
    public String toString() {
        return "ProjectionTransformation{" +
                "fovY=" + Math.toDegrees(fovY) +
                ", aspectRatio=" + aspectRatio +
                '}';
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

        return Matrix4.makeBalancedPerspectiveFrustum(right, top, zNear, zFar);
    }
}
