package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;
import com.btxtech.uiservice.renderer.ProgressAnimation;
import com.btxtech.uiservice.renderer.WebGlRenderTask;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AbstractShape3DRenderTaskRunner extends AbstractRenderTaskRunner {
    public static class BuildupState {
        private double maxZ;
        private int buildupTextureId;
        private double[] buildupMatrix;

        public BuildupState(double maxZ, int buildupTextureId) {
            this.maxZ = maxZ;
            this.buildupTextureId = buildupTextureId;
        }

        public void setBuildupMatrix(Matrix4 buildupMatrix) {
            this.buildupMatrix = buildupMatrix.toWebGlArray();
        }

        public double getMaxZ() {
            return maxZ;
        }

        public int getBuildupTextureId() {
            return buildupTextureId;
        }

        public double[] getBuildupMatrix() {
            return buildupMatrix;
        }
    }

    public interface RenderTask extends WebGlRenderTask<VertexContainer> {
        void setBuildupState(BuildupState buildupState);
    }

    protected void createShape3DRenderTasks(Shape3D shape3D, Function<Long, List<ModelMatrices>> modelMatricesSupplier) {
        createShape3DRenderTasks(shape3D, modelMatricesSupplier, null, null);
    }

    protected void createShape3DRenderTasks(Shape3D shape3D, Function<Long, List<ModelMatrices>> modelMatricesSupplier, Predicate<VertexContainer> predicate, BuildupState buildupState) {
        for (Element3D element3D : shape3D.getElement3Ds()) {
            Collection<ProgressAnimation> progressAnimations = setupProgressAnimation(element3D);
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                if (predicate != null && !predicate.test(vertexContainer)) {
                    continue;
                }
                if (buildupState != null) {
                    buildupState.setBuildupMatrix(vertexContainer.getShapeTransform().setupMatrix());
                }
                RenderTask modelRenderTask = createModelRenderTask(RenderTask.class,
                        vertexContainer,
                        modelMatricesSupplier,
                        progressAnimations,
                        vertexContainer.getShapeTransform(),
                        (mrt) -> mrt.setBuildupState(buildupState));
                modelRenderTask.setActive(true);
            }
        }
    }

    private Collection<ProgressAnimation> setupProgressAnimation(Element3D element3D) {
        if (element3D.getModelMatrixAnimations() == null) {
            return null;
        }
        return element3D.getModelMatrixAnimations().stream().map(ProgressAnimation::new).collect(Collectors.toList());
    }

}
