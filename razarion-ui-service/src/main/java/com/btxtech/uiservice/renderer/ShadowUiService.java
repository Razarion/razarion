package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Plane3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
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
        // - camera + projectionTransformation (only one which is done)
        // - highestPointInView + lowestPointInView
        // - lightDirection
        setupDepthRendererTransformations();
        shadowLookupTransformation = TEXTURE_COORDINATE_TRANSFORMATION.multiply(depthProjectionTransformation.multiply(depthViewTransformation));
    }

    public static void printDecimalPositions(String description, List<DecimalPosition> indexList) {
        System.out.println("-----------------------------------------------------------");
        System.out.println(description);
        System.out.println("List<DecimalPosition> positions = Arrays.asList(" + decimalPositionsToString(indexList) + ");");
        System.out.println("-----------------------------------------------------------");
    }

    public static String decimalPositionsToString(List<DecimalPosition> indexList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indexList.size(); i++) {
            DecimalPosition decimalPosition = indexList.get(i);
            builder.append("new DecimalPosition(").append(decimalPosition.getX()).append(", ").append(decimalPosition.getY()).append(")");
            if (i < indexList.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private void setupDepthRendererTransformations() {
        if (visualUiService.getPlanetVisualConfig().getLightDirection().getZ() >= 0.0) {
            logger.warning("setupDepthRendererTransformations(): light direction is too flat. z >= 0. LightDirection: " + visualUiService.getPlanetVisualConfig().getLightDirection());
            return;
        }
        ViewField viewFieldGroundZero = projectionTransformation.calculateViewField(0);
        if (viewFieldGroundZero.hasNullPosition()) {
            logger.warning("setupDepthRendererTransformations(): viewField.hasNullPosition() can not calculate shadow");
            return;
        }
        ViewField viewFieldHighest = projectionTransformation.calculateViewField(terrainUiService.getHighestPointInView());
        ViewField viewFieldLowest = projectionTransformation.calculateViewField(terrainUiService.getLowestPointInView());
        Vertex viewFiledCenterGroundZero = new Vertex(viewFieldGroundZero.calculateCenter(), 0);
        Plane3d planeViewFieldCenterLightDirection = new Plane3d(visualUiService.getPlanetVisualConfig().getLightDirection().negate(), viewFiledCenterGroundZero);
        Matrix4 planeRotationMatrix = Matrix4.createRotationFrom2Vectors(Vertex.Z_NORM_NEG, visualUiService.getPlanetVisualConfig().getLightDirection());
        Vertex planeXAxis = planeRotationMatrix.multiply(Vertex.X_NORM, 1);
        Vertex planeYAxis = planeRotationMatrix.multiply(Vertex.Y_NORM, 1);
        planeViewFieldCenterLightDirection.setOptionalOrigin(planeXAxis, planeYAxis);

        DecimalPosition bottomLeftPlaneLowest = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(viewFieldLowest.getBottomLeftVertex().toXY());
        DecimalPosition bottomLeftPlaneHighest = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(viewFieldHighest.getBottomLeftVertex().toXY());
        DecimalPosition bottomLeftPlane = new DecimalPosition(Math.min(bottomLeftPlaneLowest.getX(), bottomLeftPlaneHighest.getX()), Math.min(bottomLeftPlaneLowest.getY(), bottomLeftPlaneHighest.getY()));

        DecimalPosition bottomRightPlaneLowest = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(viewFieldLowest.getBottomRightVertex().toXY());
        DecimalPosition bottomRightPlaneHighest = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(viewFieldHighest.getBottomRightVertex().toXY());
        DecimalPosition bottomRightPlane = new DecimalPosition(Math.max(bottomRightPlaneLowest.getX(), bottomRightPlaneHighest.getX()), Math.min(bottomRightPlaneLowest.getY(), bottomRightPlaneHighest.getY()));

        DecimalPosition topRightPlaneLowest = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(viewFieldLowest.getTopRightVertex().toXY());
        DecimalPosition topRightPlaneHighest = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(viewFieldHighest.getTopRightVertex().toXY());
        DecimalPosition topRightPlane = new DecimalPosition(Math.max(topRightPlaneLowest.getX(), topRightPlaneHighest.getX()), Math.max(topRightPlaneLowest.getY(), topRightPlaneHighest.getY()));

        DecimalPosition topLeftPlaneLowest = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(viewFieldLowest.getTopLeftVertex().toXY());
        DecimalPosition topLeftPlaneHighest = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(viewFieldHighest.getTopLeftVertex().toXY());
        DecimalPosition topLeftPlane = new DecimalPosition(Math.min(topLeftPlaneLowest.getX(), topLeftPlaneHighest.getX()), Math.max(topLeftPlaneLowest.getY(), topLeftPlaneHighest.getY()));

        double right = Math.max(bottomLeftPlane.getDistance(bottomRightPlane), topLeftPlane.getDistance(topRightPlane)) / 2.0;
        double top = Math.max(bottomLeftPlane.getDistance(topLeftPlane), bottomRightPlane.getDistance(topRightPlane)) / 2.0;

        Vertex lowestPlaneViewFieldCenterLightVector = planeViewFieldCenterLightDirection.calculateAbsoluteMostNegZ(bottomLeftPlane, bottomRightPlane, topRightPlane, topLeftPlane).sub(0, 0, terrainUiService.getHighestPointInView());
        double centerZNearDistance = lowestPlaneViewFieldCenterLightVector.getZ() / visualUiService.getPlanetVisualConfig().getLightDirection().getZ();

        Vertex highestPlaneViewFieldCenterLightDirection = planeViewFieldCenterLightDirection.calculateAbsoluteMostPosZ(bottomLeftPlane, bottomRightPlane, topRightPlane, topLeftPlane).sub(0, 0, terrainUiService.getLowestPointInView());
        double centerZFarDistance = highestPlaneViewFieldCenterLightDirection.getZ() / -visualUiService.getPlanetVisualConfig().getLightDirection().getZ();

        double zFar = Z_NEAR + centerZNearDistance + centerZFarDistance;

        Vertex lightPositionViewFieldCenter = planeViewFieldCenterLightDirection.toAbsolute(bottomLeftPlane.add(right, top));
        Vertex lightPosition = lightPositionViewFieldCenter.add(visualUiService.getPlanetVisualConfig().getLightDirection().multiply(-Z_NEAR - centerZNearDistance));

        depthProjectionTransformation = Matrix4.makeBalancedOrthographicFrustum(right, top, Z_NEAR, zFar);

        depthViewTransformation = planeRotationMatrix.multiply(Matrix4.createTranslation(-lightPosition.getX(), -lightPosition.getY(), -lightPosition.getZ()));
    }
}
