package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Plane3d;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.VisualUiService;

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
    private static final int VISIBLE_ZONE_HEIGHT_FALLBACK = 1;
    private static final Matrix4 TEXTURE_COORDINATE_TRANSFORMATION = Matrix4.makeTextureCoordinateTransformation();
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private ProjectionTransformation projectionTransformation;
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

//    public static void printDecimalPositions(String description, List<DecimalPosition> indexList) {
//        System.out.println("-----------------------------------------------------------");
//        System.out.println(description);
//        System.out.println("List<DecimalPosition> positions = Arrays.asList(" + decimalPositionsToString(indexList) + ");");
//        System.out.println("-----------------------------------------------------------");
//    }

//    public static String decimalPositionsToString(List<DecimalPosition> indexList) {
//        StringBuilder builder = new StringBuilder();
//        for (int i = 0; i < indexList.size(); i++) {
//            DecimalPosition decimalPosition = indexList.get(i);
//            builder.append("new DecimalPosition(").append(decimalPosition.getX()).append(", ").append(decimalPosition.getY()).append(")");
//            if (i < indexList.size() - 1) {
//                builder.append(", ");
//            }
//        }
//        return builder.toString();
//    }

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

        ViewField aabbViewFiledCenterGroundZero = viewFieldGroundZero.calculateAabb();
        Vertex viewFieldCenterGroundZero = new Vertex(aabbViewFiledCenterGroundZero.calculateCenter(), 0);
        Plane3d planeViewFieldCenterLightDirection = new Plane3d(visualUiService.getPlanetVisualConfig().getLightDirection().negate(), viewFieldCenterGroundZero);
        Matrix4 planeRotationMatrix = Matrix4.createRotationFrom2Vectors(Vertex.Z_NORM_NEG, visualUiService.getPlanetVisualConfig().getLightDirection());
        Vertex planeXAxis = planeRotationMatrix.multiply(Vertex.X_NORM, 1);
        Vertex planeYAxis = planeRotationMatrix.multiply(Vertex.Y_NORM, 1);
        planeViewFieldCenterLightDirection.setOptionalOrigin(planeXAxis, planeYAxis);

        DecimalPosition bottomLeftPlane = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(aabbViewFiledCenterGroundZero.getBottomLeftVertex().toXY());
        DecimalPosition bottomRightPlane = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(aabbViewFiledCenterGroundZero.getBottomRightVertex().toXY());
        DecimalPosition topRightPlane = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(aabbViewFiledCenterGroundZero.getTopRightVertex().toXY());
        DecimalPosition topLeftPlane = planeViewFieldCenterLightDirection.perpendicularZProjectPlaneCoordinates(aabbViewFiledCenterGroundZero.getTopLeftVertex().toXY());
        Rectangle2D rectPlane = Rectangle2D.generateRectangleFromAnyPoints(bottomLeftPlane, bottomRightPlane, topRightPlane, topLeftPlane);

        double visibleZoneHeight = planeViewFieldCenterLightDirection.toAbsolute(rectPlane.cornerBottomLeft()).sub(aabbViewFiledCenterGroundZero.getBottomLeftVertex()).magnitude();
        visibleZoneHeight = Math.max(visibleZoneHeight, planeViewFieldCenterLightDirection.toAbsolute(rectPlane.cornerBottomRight()).sub(aabbViewFiledCenterGroundZero.getBottomRightVertex()).magnitude());
        visibleZoneHeight = Math.max(visibleZoneHeight, planeViewFieldCenterLightDirection.toAbsolute(rectPlane.cornerTopRight()).sub(aabbViewFiledCenterGroundZero.getTopRightVertex()).magnitude());
        visibleZoneHeight = Math.max(visibleZoneHeight, planeViewFieldCenterLightDirection.toAbsolute(rectPlane.cornerTopLeft()).sub(aabbViewFiledCenterGroundZero.getTopLeftVertex()).magnitude());
        visibleZoneHeight *= 2.0;
        visibleZoneHeight = Math.max(VISIBLE_ZONE_HEIGHT_FALLBACK, visibleZoneHeight);

        depthProjectionTransformation = Matrix4.makeBalancedOrthographicFrustum(rectPlane.width() / 2.0, rectPlane.height() / 2.0, Z_NEAR, Z_NEAR + visibleZoneHeight);

        Vertex lightPosition = viewFieldCenterGroundZero.add(visualUiService.getPlanetVisualConfig().getLightDirection().multiply(-Z_NEAR - visibleZoneHeight / 2.0));

        Matrix4 reverseRotationMatrix = Matrix4.createRotationFrom2Vectors(visualUiService.getPlanetVisualConfig().getLightDirection(), Vertex.Z_NORM_NEG);
        depthViewTransformation = reverseRotationMatrix.multiply(Matrix4.createTranslation(-lightPosition.getX(), -lightPosition.getY(), -lightPosition.getZ()));
    }
}
