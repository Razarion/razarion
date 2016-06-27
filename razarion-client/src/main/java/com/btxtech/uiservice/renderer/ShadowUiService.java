package com.btxtech.uiservice.renderer;

import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Plane3d;
import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 23.06.2015.
 */
@Singleton
@Bindable
public class ShadowUiService {
    private Logger logger = Logger.getLogger(ShadowUiService.class.getName());
    private static final Matrix4 TEXTURE_COORDINATE_TRANSFORMATION = new Matrix4(new double[][]{
            {0.5, 0.0, 0.0, 0.5},
            {0.0, 0.5, 0.0, 0.5},
            {0.0, 0.0, 0.5, 0.5},
            {0.0, 0.0, 0.0, 1.0}});
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private TerrainSurface terrainSurface;
    private double shadowAlpha = 0.2;
    private double rotateX = Math.toRadians(25);
    private double rotateZ = Math.toRadians(250);
    @Deprecated
    private double ambientIntensity;
    @Deprecated
    private double diffuseIntensity;

    // ------------------------------------------------------------------------------------------------------------

    @Deprecated
    public double getAmbientIntensity() {
        return ambientIntensity;
    }

    @Deprecated
    public void setAmbientIntensity(double ambientIntensity) {
        this.ambientIntensity = ambientIntensity;
    }

    @Deprecated
    public double getDiffuseIntensity() {
        return diffuseIntensity;
    }

    @Deprecated
    public void setDiffuseIntensity(double diffuseIntensity) {
        this.diffuseIntensity = diffuseIntensity;
    }

    // ------------------------------------------------------------------------------------------------------------

    /**
     * Return the point on the surface pointing to the sun
     *
     * @return direction normalized
     */
    public Vertex getLightDirection() {
        return Matrix4.createZRotation(rotateZ).multiply(Matrix4.createXRotation(rotateX)).multiply(new Vertex(0, 0, -1), 1.0);
    }


    public double getShadowAlpha() {
        return shadowAlpha;
    }

    public void setShadowAlpha(double shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
    }

    public double getRotateX() {
        return rotateX;
    }

    public void setRotateX(double rotateX) {
        this.rotateX = rotateX;
    }

    public double getRotateZ() {
        return rotateZ;
    }

    public void setRotateZ(double rotateZ) {
        this.rotateZ = rotateZ;
    }

    public Matrix4 createShadowLookupTransformation() {
        return TEXTURE_COORDINATE_TRANSFORMATION.multiply(createDepthProjectionTransformation().multiply(createDepthViewTransformation()));
    }

    public Matrix4 createDepthProjectionTransformation() {
        ViewField viewField = projectionTransformation.calculateViewField(0).calculateAabb();

        Vertex lightNorm = getLightDirection();
        Plane3d plane = calculatePlane(viewField, lightNorm);

        Vertex bottomLeftVertex = plane.project(viewField.getBottomLeftVertex());
        Vertex bottomRightVertex = plane.project(viewField.getBottomRightVertex());
        Vertex topRightVertex = plane.project(viewField.getTopRightVertex());
        Vertex topLeftVertex = plane.project(viewField.getTopLeftVertex());

        double right = bottomLeftVertex.distance(bottomRightVertex) / 2.0;
        double top = bottomLeftVertex.distance(topLeftVertex) / 2.0;

        double zFar = getDistance(bottomLeftVertex, lightNorm, 0);
        zFar = getDistance(bottomRightVertex, lightNorm, zFar);
        zFar = getDistance(topRightVertex, lightNorm, zFar);
        zFar = getDistance(topLeftVertex, lightNorm, zFar);

        return makeBalancedOrthographicFrustum(right, top, 10, zFar);
    }

    private Plane3d calculatePlane(ViewField viewField, Vertex lightNorm) {
        double m = terrainSurface.getHighestPointInView() / lightNorm.getZ();
        Vertex negLightNorm = lightNorm.multiply(m);

        Vertex pointOnPlane;
        if(lightNorm.getX() >= 0) {
            if(lightNorm.getY() > 0) {
                pointOnPlane = viewField.getBottomLeftVertex().add(negLightNorm);
            } else {
                pointOnPlane = viewField.getTopLeftVertex().add(negLightNorm);
            }
        } else {
            if(lightNorm.getY() > 0) {
                pointOnPlane = viewField.getBottomRightVertex().add(negLightNorm);
            } else {
                pointOnPlane = viewField.getTopRightVertex().add(negLightNorm);
            }
        }

        return new Plane3d(lightNorm, pointOnPlane);
    }

    private double getDistance(Vertex position, Vertex norm, double zFar) {
        double t = (terrainSurface.getLowestPointInView() - position.getZ()) / norm.getZ();
        Vertex worldPosition = position.add(norm.multiply(t));
        double distance = worldPosition.distance(position);
        return Math.max(distance, zFar);
    }

    public Matrix4 createDepthViewTransformation() {
        ViewField viewField = projectionTransformation.calculateViewField(0).calculateAabb();

        Vertex lightNorm = getLightDirection();
        Plane3d plane = calculatePlane(viewField, lightNorm);

        Vertex bottomLeftVertex = plane.project(viewField.getBottomLeftVertex());
        Vertex topRightVertex = plane.project(viewField.getTopRightVertex());

        double distance = bottomLeftVertex.distance(topRightVertex);
        Vertex lightPosition = bottomLeftVertex.add(topRightVertex.sub(bottomLeftVertex).normalize(distance / 2.0));

        return Matrix4.createXRotation(-rotateX).multiply(Matrix4.createZRotation(-rotateZ)).multiply(Matrix4.createTranslation(-lightPosition.getX(), -lightPosition.getY(), -lightPosition.getZ()));
    }

    public ViewField calculateViewField() {
        return projectionTransformation.calculateViewField(0).calculateAabb();
    }

    /**
     * http://www.songho.ca/opengl/gl_projectionmatrix.html
     */
    public static Matrix4 makeBalancedOrthographicFrustum(double right, double top, double zNear, double zFar) {
        double a = -2.0 / (zFar - zNear);
        double b = -(zFar + zNear) / (zFar - zNear);

        return new Matrix4(new double[][]{
                {1.0 / right, 0, 0, 0},
                {0, 1.0 / top, 0, 0},
                {0, 0, a, b},
                {0, 0, 0, 1}});
    }
}
