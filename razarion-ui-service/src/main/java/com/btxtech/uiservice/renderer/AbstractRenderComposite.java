package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.ShapeTransform;

import java.util.Collection;
import java.util.List;
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
    private D dernderData;
    private ShapeTransform shapeTransform;
    private Collection<ProgressAnimation> progressAnimations;
    @Deprecated
    private int id;

    public void init(D renderModel) {
        this.dernderData = renderModel;
    }

    public D getDernderData() {
        return dernderData;
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
        if(modelMatrixAnimations != null) {
            progressAnimations = modelMatrixAnimations.stream().map(ProgressAnimation::new).collect(Collectors.toList());
        }
        this.shapeTransform = shapeTransform;
    }

    @Deprecated
    public void setId(int id) {
        this.id = id;
    }

    public ModelMatrices mixTransformation(ModelMatrices modelMatrix) {
        if(shapeTransform == null) {
            return modelMatrix;
        }

        if (progressAnimations == null) {
            Matrix4 matrix = shapeTransform.setupMatrix();
            return modelMatrix.multiply(matrix, matrix.normTransformation());
        } else {
            ShapeTransform shapeTransformTRS = shapeTransform.copyTRS();
            for (ProgressAnimation progressAnimation : progressAnimations) {
                if (progressAnimation.isItemTriggered()) {
                    progressAnimation.dispatch(shapeTransformTRS, modelMatrix.getSyncBaseItem().getSpawnProgress());
                } else {
                    throw new UnsupportedOperationException();
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
            renderUnit.fillBuffers(dernderData);
        }
        if (depthBufferRenderUnit != null) {
            depthBufferRenderUnit.fillBuffers(dernderData);
        }
        if (wireRenderUnit != null) {
            wireRenderUnit.fillBuffers(dernderData);
        }
    }

    public void fillNormBuffer() {
        if (normRenderUnit != null) {
            normRenderUnit.fillBuffers(dernderData);
        }
    }

    @Deprecated
    public int getId() {
        return id;
    }
}
