package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Plane3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 23.06.2015.
 */
@ApplicationScoped
public class ShadowUiService {
    private Logger logger = Logger.getLogger(ShadowUiService.class.getName());
    private static final double Z_NEAR = 1;
    private static final Matrix4 TEXTURE_COORDINATE_TRANSFORMATION = new Matrix4(new double[][]{
            {0.5, 0.0, 0.0, 0.5},
            {0.0, 0.5, 0.0, 0.5},
            {0.0, 0.0, 0.5, 0.5},
            {0.0, 0.0, 0.0, 1.0}});
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private TerrainUiService terrainUiService;
    private Vertex lightDirection = Vertex.Z_NORM_NEG;
    private Matrix4 shadowLookupTransformation;
    private Matrix4 depthProjectionTransformation;
    private Matrix4 depthViewTransformation;

    public void onVisualConfig(@Observes VisualConfig visualConfig) {
        setupLightDirection(visualConfig);
    }

    public double getShadowAlpha() {
        return visualUiService.getVisualConfig().getShadowAlpha();
    }

    public Matrix4 getShadowLookupTransformation() {
        return shadowLookupTransformation;
    }

    public Vertex getLightDirection() {
        return lightDirection;
    }

    public Matrix4 getDepthProjectionTransformation() {
        return depthProjectionTransformation;
    }

    public Matrix4 getDepthViewTransformation() {
        return depthViewTransformation;
    }

    public void setupMatrices() {
        // depends on
        // - projectionTransformation (only one which is done)
        // - lightDirection
        // - terrainUiService.getHighestPointInView()
        // - visualUiService.getVisualConfig().getShadowRotationX + Y
        setupLightDirection(visualUiService.getVisualConfig());
        setupDepthProjectionTransformation();
        createDepthViewTransformation();
        shadowLookupTransformation = TEXTURE_COORDINATE_TRANSFORMATION.multiply(depthProjectionTransformation.multiply(depthViewTransformation));
    }

    /**
     * Return X axis of the light pane
     *
     * @return direction normalized
     */
    private Vertex getPlaneXAxis() {
        return Matrix4.createZRotation(visualUiService.getVisualConfig().getShadowRotationZ()).multiply(Matrix4.createXRotation(visualUiService.getVisualConfig().getShadowRotationX())).multiply(new Vertex(1, 0, 0), 1.0);
    }

    /**
     * Return Y axis of the light pane
     *
     * @return direction normalized
     */
    private Vertex getPlaneYAxis() {
        return Matrix4.createZRotation(visualUiService.getVisualConfig().getShadowRotationZ()).multiply(Matrix4.createXRotation(visualUiService.getVisualConfig().getShadowRotationX())).multiply(new Vertex(0, 1, 0), 1.0);
    }

    private void setupDepthProjectionTransformation() {
        ViewField viewField = projectionTransformation.calculateViewField(0);
        if (viewField.hasNullPosition()) {
            logger.warning("setupDepthProjectionTransformation(): viewField.hasNullPosition() can not calculate shadow");
            return;
        }
        viewField = viewField.calculateAabb();

        Vertex lightNorm = getLightDirection();
        Plane3d plane = calculatePlane(viewField, lightNorm);

        Vertex bottomLeftVertex = plane.project(viewField.getBottomLeftVertex());
        Vertex bottomRightVertex = plane.project(viewField.getBottomRightVertex());
        Vertex topRightVertex = plane.project(viewField.getTopRightVertex());
        Vertex topLeftVertex = plane.project(viewField.getTopLeftVertex());

        // Origin
        double distance = bottomLeftVertex.distance(topRightVertex);
        Vertex lightPosition = bottomLeftVertex.add(topRightVertex.sub(bottomLeftVertex).normalize(distance / 2.0));
        double m = -lightPosition.getZ() / lightNorm.getZ();
        Vertex lightPositionZeroGround = lightPosition.add(lightNorm.multiply(m));
        plane.setOptionalOrigin(lightPositionZeroGround, getPlaneXAxis(), getPlaneYAxis());

        DecimalPosition bottomLeftPlane = plane.getPlaneCoordinates(bottomLeftVertex);
        DecimalPosition bottomRightPlane = plane.getPlaneCoordinates(bottomRightVertex);
        DecimalPosition topRightPlane = plane.getPlaneCoordinates(topRightVertex);
        DecimalPosition topLeftPlane = plane.getPlaneCoordinates(topLeftVertex);
        DecimalPosition biggest = DecimalPosition.getBiggestAabb(bottomLeftPlane, bottomRightPlane, topRightPlane, topLeftPlane);
        DecimalPosition smallest = DecimalPosition.getSmallestAabb(bottomLeftPlane, bottomRightPlane, topRightPlane, topLeftPlane);
        DecimalPosition delta = biggest.sub(smallest);

        double zFar = getDistance(bottomLeftVertex, lightNorm, 0);
        zFar = getDistance(bottomRightVertex, lightNorm, zFar);
        zFar = getDistance(topRightVertex, lightNorm, zFar);
        zFar = getDistance(topLeftVertex, lightNorm, zFar);

        depthProjectionTransformation = makeBalancedOrthographicFrustum(delta.getX() / 2.0, delta.getY() / 2.0, Z_NEAR, Z_NEAR + zFar);
    }

    private Plane3d calculatePlane(ViewField viewField, Vertex lightNorm) {
        double m = terrainUiService.getHighestPointInView() / lightNorm.getZ();
        Vertex negLightNorm = lightNorm.multiply(m);

        Vertex pointOnPlane;
        if (lightNorm.getX() >= 0) {
            if (lightNorm.getY() > 0) {
                pointOnPlane = viewField.getBottomLeftVertex().add(negLightNorm);
            } else {
                pointOnPlane = viewField.getTopLeftVertex().add(negLightNorm);
            }
        } else {
            if (lightNorm.getY() > 0) {
                pointOnPlane = viewField.getBottomRightVertex().add(negLightNorm);
            } else {
                pointOnPlane = viewField.getTopRightVertex().add(negLightNorm);
            }
        }

        return new Plane3d(lightNorm, pointOnPlane);
    }

    private double getDistance(Vertex position, Vertex norm, double zFar) {
        double t = (terrainUiService.getLowestPointInView() - position.getZ()) / norm.getZ();
        Vertex worldPosition = position.add(norm.multiply(t));
        double distance = worldPosition.distance(position);
        return Math.max(distance, zFar);
    }

    private void createDepthViewTransformation() {
        ViewField viewField = projectionTransformation.calculateViewField(0);
        if (viewField.hasNullPosition()) {
            logger.warning("createDepthViewTransformation(): viewField.hasNullPosition() can not calculate shadow");
            return;
        }
        viewField = viewField.calculateAabb();

        Vertex lightNorm = getLightDirection();
        Plane3d plane = calculatePlane(viewField, lightNorm);

        Vertex bottomLeftVertex = plane.project(viewField.getBottomLeftVertex());
        Vertex topRightVertex = plane.project(viewField.getTopRightVertex());

        double distance = bottomLeftVertex.distance(topRightVertex);
        Vertex lightPosition = bottomLeftVertex.add(topRightVertex.sub(bottomLeftVertex).normalize(distance / 2.0));

        depthViewTransformation = Matrix4.createXRotation(-visualUiService.getVisualConfig().getShadowRotationX()).multiply(Matrix4.createZRotation(-visualUiService.getVisualConfig().getShadowRotationZ())).multiply(Matrix4.createTranslation(-lightPosition.getX(), -lightPosition.getY(), -lightPosition.getZ()));
    }

    private void setupLightDirection(VisualConfig visualConfig) {
        lightDirection = Matrix4.createZRotation(visualConfig.getShadowRotationZ()).multiply(Matrix4.createXRotation(visualConfig.getShadowRotationX())).multiply(new Vertex(0, 0, -1), 1.0);
    }

    public ViewField calculateViewField() {
        return projectionTransformation.calculateViewField(0).calculateAabb();
    }

    /**
     * http://www.songho.ca/opengl/gl_projectionmatrix.html
     */
    private Matrix4 makeBalancedOrthographicFrustum(double right, double top, double zNear, double zFar) {
        double a = -2.0 / (zFar - zNear);
        double b = -(zFar + zNear) / (zFar - zNear);

        return new Matrix4(new double[][]{
                {1.0 / right, 0, 0, 0},
                {0, 1.0 / top, 0, 0},
                {0, 0, a, b},
                {0, 0, 0, 1}});
    }
}
