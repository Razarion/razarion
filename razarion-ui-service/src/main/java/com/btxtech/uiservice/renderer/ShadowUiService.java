package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Plane3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.utils.MathHelper;
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
    private static final double Z_NEAR = 10;
    private static final Matrix4 TEXTURE_COORDINATE_TRANSFORMATION = Matrix4.makeTextureCoordinateTransformation();
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private TerrainUiService terrainUiService;
    private Vertex lightDirection = Vertex.Z_NORM_NEG;
    private Matrix4 shadowLookupTransformation;
    private Matrix4 depthProjectionTransformation;
    private Matrix4 depthViewTransformation;

    public void onVisualConfig(@Observes PlanetVisualConfig planetVisualConfig) {
        setupLightDirection(planetVisualConfig);
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
        // - visualUiService.getStaticVisualConfig().getShadowRotationX + Y
        setupLightDirection(visualUiService.getPlanetVisualConfig());
        setupDepthRendererTransformations();
        // setupDepthRendererViewTransformation();
        shadowLookupTransformation = TEXTURE_COORDINATE_TRANSFORMATION.multiply(depthProjectionTransformation.multiply(depthViewTransformation));
    }

    /**
     * Return X axis of the light pane
     *
     * @return direction normalized
     */
    private Vertex getPlaneXAxis() {
        return Matrix4.createYRotation(visualUiService.getPlanetVisualConfig().getShadowRotationY()).multiply(Matrix4.createXRotation(visualUiService.getPlanetVisualConfig().getShadowRotationX())).multiply(new Vertex(1, 0, 0), 1.0);
    }

    /**
     * Return Y axis of the light pane
     *
     * @return direction normalized
     */
    private Vertex getPlaneYAxis() {
        return Matrix4.createYRotation(visualUiService.getPlanetVisualConfig().getShadowRotationY()).multiply(Matrix4.createXRotation(visualUiService.getPlanetVisualConfig().getShadowRotationX())).multiply(new Vertex(0, 1, 0), 1.0);
    }

    private void setupDepthRendererTransformations() {
        ViewField viewField = projectionTransformation.calculateViewField(0);
        if (viewField.hasNullPosition()) {
            logger.warning("setupDepthRendererTransformations(): viewField.hasNullPosition() can not calculate shadow");
            return;
        }
        viewField = viewField.calculateAabb();

        Plane3d plane = calculatePlane(viewField);

        Vertex bottomLeftVertex = plane.project(viewField.getBottomLeftVertex());
        Vertex bottomRightVertex = plane.project(viewField.getBottomRightVertex());
        Vertex topRightVertex = plane.project(viewField.getTopRightVertex());
        Vertex topLeftVertex = plane.project(viewField.getTopLeftVertex());

        // Origin
        double distance = bottomLeftVertex.distance(topRightVertex);
        if (MathHelper.compareWithPrecision(distance, 0.0)) {
            logger.warning("setupDepthRendererTransformations(): distance is too small. Light is coming from a very flat angle.");
            return;
        }
        Vertex lightPositionOnPlane = bottomLeftVertex.add(topRightVertex.sub(bottomLeftVertex).normalize(distance / 2.0));
        double m = -lightPositionOnPlane.getZ() / lightDirection.getZ();
        Vertex lightPositionZeroGround = lightPositionOnPlane.add(lightDirection.multiply(m));
        plane.setOptionalOrigin(lightPositionZeroGround, getPlaneXAxis(), getPlaneYAxis());

        DecimalPosition bottomLeftPlane = plane.getPlaneCoordinates(bottomLeftVertex);
        DecimalPosition bottomRightPlane = plane.getPlaneCoordinates(bottomRightVertex);
        DecimalPosition topRightPlane = plane.getPlaneCoordinates(topRightVertex);
        DecimalPosition topLeftPlane = plane.getPlaneCoordinates(topLeftVertex);
        double right = Math.max(bottomLeftPlane.getDistance(bottomRightPlane), topRightPlane.getDistance(topLeftPlane)) / 2.0;
        double top = Math.max(bottomLeftPlane.getDistance(topLeftPlane), bottomRightPlane.getDistance(topRightPlane)) / 2.0;
        double edge = Math.max(right, top);

        double distanceZero2Plane = lightPositionOnPlane.distance(lightPositionZeroGround);
        double distanceTerrainLowest = calculateDistanceTerrainLowest(viewField.getBottomLeftVertex(), bottomLeftVertex, 0);
        distanceTerrainLowest = calculateDistanceTerrainLowest(viewField.getBottomRightVertex(), bottomRightVertex, distanceTerrainLowest);
        distanceTerrainLowest = calculateDistanceTerrainLowest(viewField.getTopRightVertex(), topRightVertex, distanceTerrainLowest);
        distanceTerrainLowest = calculateDistanceTerrainLowest(viewField.getTopLeftVertex(), topLeftVertex, distanceTerrainLowest);

        double distanceZNear2ZFar = distanceZero2Plane + distanceTerrainLowest;

        depthProjectionTransformation = Matrix4.makeBalancedOrthographicFrustum(edge, edge, Z_NEAR, Z_NEAR + distanceZNear2ZFar);

        Vertex lightPosition = lightPositionOnPlane.add(lightDirection.multiply(-Z_NEAR));

        depthViewTransformation = Matrix4.createXRotation(-visualUiService.getPlanetVisualConfig().getShadowRotationX()).multiply(Matrix4.createYRotation(-visualUiService.getPlanetVisualConfig().getShadowRotationY())).multiply(Matrix4.createTranslation(-lightPosition.getX(), -lightPosition.getY(), -lightPosition.getZ()));

    }

    private double calculateDistanceTerrainLowest(Vertex zeroPosition, Vertex planePosition, double zFar) {
        Vertex subTerrainVector = lightDirection.multiply(terrainUiService.getLowestPointInView() / lightDirection.getZ());
        return Math.max(zeroPosition.add(subTerrainVector).distance(planePosition), zFar);
    }


    private Plane3d calculatePlane(ViewField viewField) {
        double m = terrainUiService.getHighestPointInView() / lightDirection.getZ();
        Vertex positionOnLightDirectionWithHighestPointInView = lightDirection.multiply(m);

        Vertex pointOnPlane;
        // Find the corner which touches ground at height at zero
        if (lightDirection.getX() >= 0) {
            if (lightDirection.getY() > 0) {
                pointOnPlane = viewField.getBottomLeftVertex().add(positionOnLightDirectionWithHighestPointInView);
            } else {
                pointOnPlane = viewField.getTopLeftVertex().add(positionOnLightDirectionWithHighestPointInView);
            }
        } else {
            if (lightDirection.getY() > 0) {
                pointOnPlane = viewField.getBottomRightVertex().add(positionOnLightDirectionWithHighestPointInView);
            } else {
                pointOnPlane = viewField.getTopRightVertex().add(positionOnLightDirectionWithHighestPointInView);
            }
        }

        return new Plane3d(lightDirection, pointOnPlane);
    }

    private void setupLightDirection(PlanetVisualConfig planetVisualConfig) {
        lightDirection = Matrix4.createYRotation(planetVisualConfig.getShadowRotationY()).multiply(Matrix4.createXRotation(planetVisualConfig.getShadowRotationX())).multiply(new Vertex(0, 0, -1), 1.0);
    }

}
