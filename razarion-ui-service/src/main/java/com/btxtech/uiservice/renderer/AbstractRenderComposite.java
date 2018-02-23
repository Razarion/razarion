package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.nativejs.NativeMatrix;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.datatypes.ModelMatrices;

import javax.inject.Inject;
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
    private NativeMatrix staticShapeTransformCache;
    private Collection<ProgressAnimation> progressAnimations;
    private ModelRenderer modelRenderer;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;

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

    public void setupNoAnimation(ShapeTransform shapeTransform) {
        this.shapeTransform = shapeTransform;
    }

    public ModelMatrices mixTransformation(ModelMatrices modelMatrix, double interpolationFactor) {
        modelMatrix = modelMatrix.interpolateVelocity(interpolationFactor);

        if (shapeTransform == null) {
            return modelMatrix.calculateFromTurretAngle();
        }

        if (progressAnimations == null) {
            if (shapeTransform.getStaticMatrix() != null) {
                if (staticShapeTransformCache == null) {
                    staticShapeTransformCache = nativeMatrixFactory.createFromColumnMajorArray(shapeTransform.getStaticMatrix().toWebGlArray());
                }
                return modelMatrix.multiplyStaticShapeTransform(staticShapeTransformCache).calculateFromTurretAngle();
            } else {
                return modelMatrix.multiplyShapeTransform(shapeTransform).calculateFromTurretAngle();
            }
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
            return modelMatrix.multiplyShapeTransform(shapeTransformTRS).calculateFromTurretAngle();
        }
    }

    private double setupContinuesAnimationProgress(ProgressAnimation progressAnimation) {
        int millis = (int) (System.currentTimeMillis() % progressAnimation.getTotalTime());
        return (double) millis / (double) progressAnimation.getTotalTime();
    }

    public void prepareDraw() {

    }

    public void draw(List<ModelMatrices> modelMatrices, double interpolationFactor) {
        if (renderUnit == null || !renderUnit.hasElements()) {
            return;
        }
        draw(renderUnit, modelMatrices, interpolationFactor);
    }

    public void drawDepthBuffer(List<ModelMatrices> modelMatrices, double interpolationFactor) {
        if (depthBufferRenderUnit == null || !depthBufferRenderUnit.hasElements()) {
            return;
        }
        draw(depthBufferRenderUnit, modelMatrices, interpolationFactor);
    }

    public void drawNorm(List<ModelMatrices> modelMatrices, double interpolationFactor) {
        if (normRenderUnit == null || !normRenderUnit.hasElements()) {
            return;
        }
        draw(normRenderUnit, modelMatrices, interpolationFactor);
    }

    protected void draw(AbstractRenderUnit renderUnit, List<ModelMatrices> modelMatrices, double interpolationFactor) {
        renderUnit.prepareDraw();

        if (modelMatrices != null) {
            for (ModelMatrices modelMatrice : modelMatrices) {
                renderUnit.draw(mixTransformation(modelMatrice, interpolationFactor));
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

    public ModelRenderer getModelRenderer() {
        return modelRenderer;
    }

    public void setModelRenderer(ModelRenderer modelRenderer) {
        this.modelRenderer = modelRenderer;
    }

    public void dispose() {
        if (renderUnit != null) {
            renderUnit.dispose();
        }
        if (depthBufferRenderUnit != null) {
            depthBufferRenderUnit.dispose();
        }
        if (wireRenderUnit != null) {
            wireRenderUnit.dispose();
        }
        if (normRenderUnit != null) {
            normRenderUnit.dispose();
        }
    }

}
