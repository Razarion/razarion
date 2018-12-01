package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Plane3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.ApplicationScoped;
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
    private Matrix4 shadowLookupTransformation;
    private Matrix4 depthProjectionTransformation;
    private Matrix4 depthViewTransformation;

    public Matrix4 getShadowLookupTransformation() {
        return shadowLookupTransformation;
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
        setupDepthRendererTransformations();
        // setupDepthRendererViewTransformation();
        shadowLookupTransformation = TEXTURE_COORDINATE_TRANSFORMATION.multiply(depthProjectionTransformation.multiply(depthViewTransformation));
    }

    private void setupDepthRendererTransformations() {
        if (visualUiService.getPlanetVisualConfig().getLightDirection().getZ() >= 0.0) {
            logger.warning("setupDepthRendererTransformations(): light direction is too flat. z >= 0. LightDirection: " + visualUiService.getPlanetVisualConfig().getLightDirection());
            return;
        }
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
        double m = -lightPositionOnPlane.getZ() / visualUiService.getPlanetVisualConfig().getLightDirection().getZ();
        Vertex lightPositionZeroGround = lightPositionOnPlane.add(visualUiService.getPlanetVisualConfig().getLightDirection().multiply(m));
        Matrix4 planeRotationMatrix = Matrix4.createRotationFrom2Vectors(Vertex.Z_NORM_NEG, visualUiService.getPlanetVisualConfig().getLightDirection());
        Vertex planeXAxis = planeRotationMatrix.multiply(Vertex.X_NORM, 1);
        Vertex planeYAxis = planeRotationMatrix.multiply(Vertex.Y_NORM, 1);
        plane.setOptionalOrigin(lightPositionZeroGround, planeXAxis, planeYAxis);

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

        Vertex lightPosition = lightPositionOnPlane.add(visualUiService.getPlanetVisualConfig().getLightDirection().multiply(-Z_NEAR));

        depthViewTransformation = planeRotationMatrix.multiply(Matrix4.createTranslation(-lightPosition.getX(), -lightPosition.getY(), -lightPosition.getZ()));
    }

    private double calculateDistanceTerrainLowest(Vertex zeroPosition, Vertex planePosition, double zFar) {
        Vertex subTerrainVector = visualUiService.getPlanetVisualConfig().getLightDirection().multiply(terrainUiService.getLowestPointInView() / visualUiService.getPlanetVisualConfig().getLightDirection().getZ());
        return Math.max(zeroPosition.add(subTerrainVector).distance(planePosition), zFar);
    }


    private Plane3d calculatePlane(ViewField viewField) {
        double m = terrainUiService.getHighestPointInView() / visualUiService.getPlanetVisualConfig().getLightDirection().getZ();
        Vertex positionOnLightDirectionWithHighestPointInView = visualUiService.getPlanetVisualConfig().getLightDirection().multiply(m);

        Vertex pointOnPlane;
        // Find the corner which touches ground at height at zero
        if (visualUiService.getPlanetVisualConfig().getLightDirection().getX() >= 0) {
            if (visualUiService.getPlanetVisualConfig().getLightDirection().getY() > 0) {
                pointOnPlane = viewField.getBottomLeftVertex().add(positionOnLightDirectionWithHighestPointInView);
            } else {
                pointOnPlane = viewField.getTopLeftVertex().add(positionOnLightDirectionWithHighestPointInView);
            }
        } else {
            if (visualUiService.getPlanetVisualConfig().getLightDirection().getY() > 0) {
                pointOnPlane = viewField.getBottomRightVertex().add(positionOnLightDirectionWithHighestPointInView);
            } else {
                pointOnPlane = viewField.getTopRightVertex().add(positionOnLightDirectionWithHighestPointInView);
            }
        }

        return new Plane3d(visualUiService.getPlanetVisualConfig().getLightDirection(), pointOnPlane);
    }

}
