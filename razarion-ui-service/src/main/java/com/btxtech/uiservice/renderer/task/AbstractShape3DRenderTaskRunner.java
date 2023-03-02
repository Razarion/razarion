package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;
import com.btxtech.uiservice.renderer.ProgressAnimation;
import com.btxtech.uiservice.renderer.WebGlRenderTask;
import com.btxtech.uiservice.renderer.task.progress.ProgressState;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AbstractShape3DRenderTaskRunner extends AbstractRenderTaskRunner {
    @Inject
    private AlarmService alarmService;

    public interface RenderTask extends WebGlRenderTask<VertexContainer> {
        void setProgressState(ProgressState buildupState);
    }

    protected void createShape3DRenderTasks(Shape3D shape3D, Function<Long, List<ModelMatrices>> modelMatricesSupplier) {
        createShape3DRenderTasks(shape3D, modelMatricesSupplier, null, null);
    }

    protected void createShape3DRenderTasks(Shape3D shape3D, Function<Long, List<ModelMatrices>> modelMatricesSupplier, Predicate<VertexContainer> predicate, ProgressState progressState) {
        for (Element3D element3D : shape3D.getElement3Ds()) {
            Collection<ProgressAnimation> progressAnimations = setupProgressAnimation(element3D);
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                if (vertexContainer.getVertexContainerMaterial() == null) {
                    alarmService.riseAlarm(Alarm.Type.INVALID_SHAPE_3D, "No material for: " + vertexContainer.getKey(), shape3D.getId());
                    return;
                }
                if (predicate != null && !predicate.test(vertexContainer)) {
                    continue;
                }
                createModelRenderTask(RenderTask.class,
                        vertexContainer,
                        modelMatricesSupplier,
                        progressAnimations,
                        vertexContainer.getShapeTransform(),
                        (mrt) -> mrt.setProgressState(progressState != null ? progressState.fork(vertexContainer) : null));
            }
        }
    }

    protected void createMeshRenderTask(Shape3D shape3D, String element3DId, Function<Long, List<ModelMatrices>> modelMatricesSupplier, ProgressState progressState) {
        Element3D element3D = shape3D.getElement3Ds().stream()
                .filter(e3D -> e3D.getId().equals(element3DId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No Element3D int Shape3D '" + shape3D.getId() + "' for element3DId: " + element3DId));

        System.out.println(element3DId);

        Collection<ProgressAnimation> progressAnimations = setupProgressAnimation(element3D);
        element3D.getVertexContainers().forEach(vertexContainer -> {
            if (vertexContainer.getVertexContainerMaterial() == null) {
                alarmService.riseAlarm(Alarm.Type.INVALID_SHAPE_3D, "No material for: " + vertexContainer.getKey(), shape3D.getId());
                return;
            }
            System.out.println(modelMatricesSupplier);
            createModelRenderTask(RenderTask.class,
                    vertexContainer,
                    modelMatricesSupplier,
                    progressAnimations,
                    null,
                    (mrt) -> mrt.setProgressState(progressState != null ? progressState.fork(vertexContainer) : null));
        });
    }

    private Collection<ProgressAnimation> setupProgressAnimation(Element3D element3D) {
        if (element3D.getModelMatrixAnimations() == null) {
            return null;
        }
        return element3D.getModelMatrixAnimations().stream().map(ProgressAnimation::new).collect(Collectors.toList());
    }

}
