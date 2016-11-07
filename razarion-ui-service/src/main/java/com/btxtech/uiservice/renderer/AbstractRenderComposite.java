package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.ShapeTransform;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 04.09.2015.
 * <p>
 * U AbstractRenderUnit (e.g.: AbstractVertexContainerRenderUnit)
 * D render data (e.g.: VertexContainerRender)
 */
public abstract class AbstractRenderComposite<U extends AbstractRenderUnit<D>, D> {
    private U renderUnit;
    private U depthBufferRenderUnit;
    private U wireRenderUnit;
    private U normRenderUnit;
    private D rendererData;
    private ShapeTransform shapeTransform;
    private Collection<ProgressAnimation> progressAnimations;
    @Deprecated
    private int id;

    public void init(D renderModel) {
        this.rendererData = renderModel;
    }

    public D getRendererData() {
        return rendererData;
    }

    public void setRenderUnit(U renderUnit) {
        this.renderUnit = renderUnit;
        renderUnit.setAbstractRenderComposite(this);
    }

    public void setDepthBufferRenderUnit(U depthBufferRenderUnit) {
        this.depthBufferRenderUnit = depthBufferRenderUnit;
        depthBufferRenderUnit.setAbstractRenderComposite(this);
    }

    public void setWireRenderUnit(U wireRenderUnit) {
        this.wireRenderUnit = wireRenderUnit;
        wireRenderUnit.setAbstractRenderComposite(this);
    }

    public void setNormRenderUnit(U normRenderUnit) {
        this.normRenderUnit = normRenderUnit;
        normRenderUnit.setAbstractRenderComposite(this);
    }

    public void setupAnimation(Shape3D shape3D, Element3D element3D, ShapeTransform shapeTransform) {
        Collection<ModelMatrixAnimation> modelMatrixAnimations = shape3D.setupAnimations(element3D);
        if (modelMatrixAnimations != null) {
            progressAnimations = modelMatrixAnimations.stream().map(ProgressAnimation::new).collect(Collectors.toList());
        }
        this.shapeTransform = shapeTransform;
    }

    @Deprecated
    public void setId(int id) {
        this.id = id;
    }

    public ModelMatrices mixTransformation(ModelMatrices modelMatrix) {
        if (shapeTransform == null) {
            return modelMatrix;
        }

        if (progressAnimations == null) {
            Matrix4 matrix = shapeTransform.setupMatrix();
            return modelMatrix.multiply(matrix, matrix.normTransformation());
        } else {
            ShapeTransform shapeTransformTRS = shapeTransform.copyTRS();
            for (ProgressAnimation progressAnimation : progressAnimations) {
                Objects.requireNonNull(progressAnimation.getAnimationTrigger(), "No animation trigger");
                switch (progressAnimation.getAnimationTrigger()) {
                    case ITEM_PROGRESS:
                        progressAnimation.dispatch(shapeTransformTRS, modelMatrix.getProgress());
                        break;
                    case SINGLE_RUN:
                        progressAnimation.dispatch(shapeTransformTRS, modelMatrix.getProgress());
                        break;
                    case CONTINUES:
                        progressAnimation.dispatch(shapeTransformTRS, setupContinuesAnimationProgress(progressAnimation));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown animation trigger '" + progressAnimation.getAnimationTrigger());
                }
            }
            Matrix4 matrix = shapeTransformTRS.setupMatrix();
            if (matrix.zero()) {
                return modelMatrix.multiply(matrix, matrix);
            } else {
                return modelMatrix.multiply(matrix, matrix.normTransformation());
            }
        }
    }

    private double setupContinuesAnimationProgress(ProgressAnimation progressAnimation) {
        int millis = (int) (System.currentTimeMillis() % progressAnimation.getTotalTime());
        double progress =  (double) millis / (double) progressAnimation.getTotalTime();
        return progress;
    }

    public void prepareDraw() {

    }

    public void draw(List<ModelMatrices> modelMatrices) {
        if (renderUnit == null || !renderUnit.hasElements()) {
            return;
        }
        draw(renderUnit, modelMatrices);
    }

    public void drawDepthBuffer(List<ModelMatrices> modelMatrices) {
        if (depthBufferRenderUnit == null || !depthBufferRenderUnit.hasElements()) {
            return;
        }
        draw(depthBufferRenderUnit, modelMatrices);
    }

    public void drawNorm(List<ModelMatrices> modelMatrices) {
        if (normRenderUnit == null || !normRenderUnit.hasElements()) {
            return;
        }
        draw(normRenderUnit, modelMatrices);
    }

    protected void draw(AbstractRenderUnit renderUnit, List<ModelMatrices> modelMatrices) {
        renderUnit.prepareDraw();

        if (modelMatrices != null) {
            for (ModelMatrices modelMatrice : modelMatrices) {
                renderUnit.draw(mixTransformation(modelMatrice));
            }
        } else {
            renderUnit.draw(null);
        }
    }

    @Deprecated
    public void drawWire() {
        if (wireRenderUnit != null && wireRenderUnit.hasElements()) {
            wireRenderUnit.draw(null);
        }
    }

    public void fillBuffers() {
        if (renderUnit != null) {
            renderUnit.fillBuffers(rendererData);
        }
        if (depthBufferRenderUnit != null) {
            depthBufferRenderUnit.fillBuffers(rendererData);
        }
        if (wireRenderUnit != null) {
            wireRenderUnit.fillBuffers(rendererData);
        }
    }

    public void fillNormBuffer() {
        if (normRenderUnit != null) {
            normRenderUnit.fillBuffers(rendererData);
        }
    }

    @Deprecated
    public int getId() {
        return id;
    }
}
