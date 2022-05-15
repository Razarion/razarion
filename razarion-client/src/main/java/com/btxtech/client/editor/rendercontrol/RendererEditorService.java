package com.btxtech.client.editor.rendercontrol;

import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderService;
import jsinterop.annotations.JsType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Deprecated
@JsType
@ApplicationScoped
public class RendererEditorService {
    @Inject
    private RenderService renderService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation projectionTransformation;

    @SuppressWarnings("unused") // Called by Angular
    public boolean isRenderInterpolation() {
        return renderService.isInterpolation();
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setRenderInterpolation(boolean interpolation) {
        renderService.setInterpolation(interpolation);
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean isCallGetError() {
        return WebGlUtil.isCallGetError();
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setCallGetError(boolean callGetError) {
        WebGlUtil.setCallGetError(callGetError);
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getCameraXPosition() {
        return camera.getTranslateX();
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setCameraXPosition(double x) {
        camera.setTranslateX(x);
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getCameraYPosition() {
        return camera.getTranslateY();
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setCameraYPosition(double y) {
        camera.setTranslateY(y);
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getCameraZPosition() {
        return camera.getTranslateZ();
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setCameraZPosition(double z) {
        camera.setTranslateZ(z);
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getCameraXRotation() {
        return camera.getRotateX();
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setCameraXRotation(double x) {
        camera.setRotateX(x);
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getCameraZRotation() {
        return camera.getRotateZ();
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setCameraZRotation(double z) {
        camera.setRotateZ(z);
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getCameraOpeningAngleY() {
        return projectionTransformation.getFovY();
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setCameraOpeningAngleY(double y) {
        projectionTransformation.setFovY(y);
    }

    @SuppressWarnings("unused") // Called by Angular
    public RenderTaskRunnerControl[] getRenderTaskRunnerControls() {
        return renderService.getRenderTaskRunners().stream()
                .map(RenderTaskRunnerControl::new)
                .toArray(RenderTaskRunnerControl[]::new);
    }

}
