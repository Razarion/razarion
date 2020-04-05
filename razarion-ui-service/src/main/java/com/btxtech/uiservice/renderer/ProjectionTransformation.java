package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.Matrix4;
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
    private static final double DEFAULT_FOV_Y = Math.toRadians(50);
    // private Logger logger = Logger.getLogger(ProjectionTransformation.class.getName());
    @Inject
    private Camera camera;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private ViewService viewService;
    private double fovY = DEFAULT_FOV_Y;
    private double aspectRatio = 4.0 / 3.0;
    private Matrix4 matrix;
    private double zNear;
    private double zFar;
    private boolean fovYConstrain = true;
    private Double bottomWidth;

    public double getFovY() {
        return fovY;
    }

    /**
     * Does not check the FovY constraints. Should only be used in editors.
     *
     * @param fovY field of view Y on radiants
     */
    public void setFovY(double fovY) {
        this.fovY = fovY;
        setupMatrices();
        if (viewService != null) {
            // viewService == null in tests
            viewService.onViewChanged();
        }
    }

    public void setViewFieldBottomWidth(Double bottomWidth) {
        this.bottomWidth = bottomWidth;
        if (bottomWidth != null) {
            setupFovYFromBottomWidth();
        }
    }

    public void setViewFieldBottomWidthFromCurrent() {
        double camera2BottomViewFiled = camera.getTranslateZ() / Math.cos(camera.getRotateX() - fovY / 2.0);
        double fovXHalf = Math.atan(Math.tan(fovY / 2.0) * aspectRatio);
        bottomWidth = Math.tan(fovXHalf) * camera2BottomViewFiled * 2.0;
    }

    private void setupFovYFromBottomWidth() {
        double camera2BottomViewFiled = camera.getTranslateZ() / Math.cos(camera.getRotateX() - fovY / 2.0);
        double fovX = 2.0 * Math.atan(bottomWidth / 2.0 / camera2BottomViewFiled);
        double fovY = 2.0 * Math.atan(Math.tan(fovX / 2.0) / aspectRatio);
        setConstrainedFovY(fovY);
    }

    public void setFovYSave(double fovY) {
        if (bottomWidth != null) {
            return;
        }
        setConstrainedFovY(fovY);
    }

    private void setConstrainedFovY(double fovY) {
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
        if (bottomWidth != null) {
            setupFovYFromBottomWidth();
        }
        setupMatrices();
        if (viewService != null) {
            // viewService == null in tests
            viewService.onViewChanged();
        }
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
     * @param clip effects coordinates (-1 to 1)
     */
    public Line3d createPickRay(DecimalPosition clip) {
        double top = zNear * Math.tan(fovY / 2.0);
        double y = top * clip.getY();
        double x = top * aspectRatio * clip.getX();
        return new Line3d(new Vertex(0, 0, 0), new Vertex(x, y, -zNear).normalize(1));
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
