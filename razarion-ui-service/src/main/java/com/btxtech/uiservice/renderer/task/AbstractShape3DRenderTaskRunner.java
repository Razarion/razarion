package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;
import com.btxtech.uiservice.renderer.ProgressAnimation;
import com.btxtech.uiservice.renderer.WebGlRenderTask;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbstractShape3DRenderTaskRunner extends AbstractRenderTaskRunner {

    public interface RenderTask extends WebGlRenderTask<VertexContainer> {
    }

    protected void createShape3DRenderTasks(Shape3D shape3D, Function<Long, List<ModelMatrices>> modelMatricesSupplier) {
        for (Element3D element3D : shape3D.getElement3Ds()) {
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                RenderTask modelRenderTask = createModelRenderTask(RenderTask.class,
                        vertexContainer,
                        modelMatricesSupplier,
                        setupProgressAnimation(shape3D, element3D),
                        vertexContainer.getShapeTransform());
                modelRenderTask.setActive(true);
            }
        }
    }

    private Collection<ProgressAnimation> setupProgressAnimation(Shape3D shape3D, Element3D element3D) {
        Collection<ModelMatrixAnimation> modelMatrixAnimations = shape3D.setupAnimations(element3D);
        if (modelMatrixAnimations != null) {
            return modelMatrixAnimations.stream().map(ProgressAnimation::new).collect(Collectors.toList());
        }
        return null;
    }

}
