package com.btxtech.uiservice.renderer;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.renderer.task.BaseItemRenderTask;
import com.btxtech.uiservice.renderer.task.BoxItemRenderTask;
import com.btxtech.uiservice.renderer.task.ProjectileRenderTask;
import com.btxtech.uiservice.renderer.task.ResourceItemRenderTask;
import com.btxtech.uiservice.renderer.task.TerrainObjectRenderTask;
import com.btxtech.uiservice.renderer.task.TrailRenderTask;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;
import com.btxtech.uiservice.renderer.task.itemplacer.BaseItemPlacerRenderTask;
import com.btxtech.uiservice.renderer.task.particle.ParticleRenderTask;
import com.btxtech.uiservice.renderer.task.selection.ItemMarkerRenderTask;
import com.btxtech.uiservice.renderer.task.selection.SelectionFrameRenderTask;
import com.btxtech.uiservice.renderer.task.slope.SlopeRenderTask;
import com.btxtech.uiservice.renderer.task.visualization.ItemVisualizationRenderTask;
import com.btxtech.uiservice.renderer.task.water.WaterRenderTask;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 12.07.2016.
 */
public abstract class RenderService {
    // private Logger logger = Logger.getLogger(RenderService.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<AbstractRenderTask> instance;
    @Inject
    private PerfmonService perfmonService;
    private List<AbstractRenderTask> renderTasks = new ArrayList<>();
    private boolean showNorm;

    protected abstract void internalSetup();

    protected abstract void prepareMainRendering();

    protected abstract void prepareDepthBufferRendering();

    protected abstract void prepare(RenderUnitControl renderUnitControl);

    public void setup() {
        internalSetup();
        renderTasks.clear();

        addRenderTask(GroundRenderTask.class, "Ground");
        addRenderTask(SlopeRenderTask.class, "Slope");
        addRenderTask(TerrainObjectRenderTask.class, "Terrain Object");
        addRenderTask(ItemMarkerRenderTask.class, "Item Marker");
        addRenderTask(BaseItemRenderTask.class, "Base Item");
        addRenderTask(TrailRenderTask.class, "Trail");
        addRenderTask(ResourceItemRenderTask.class, "Resource");
        addRenderTask(BoxItemRenderTask.class, "Box");
        addRenderTask(WaterRenderTask.class, "Water");
        addRenderTask(ProjectileRenderTask.class, "Projectile");
        addRenderTask(BaseItemPlacerRenderTask.class, "Base Item Placer");
        addRenderTask(SelectionFrameRenderTask.class, "Selection Frame");
        addRenderTask(ItemVisualizationRenderTask.class, "Tip");
        addRenderTask(ParticleRenderTask.class, "Particle");

        fillBuffers();
    }

    private void addRenderTask(Class<? extends AbstractRenderTask> clazz, String name) {
        addRenderTask(instance.select(clazz).get(), name);
    }

    public void addRenderTask(AbstractRenderTask abstractRenderTask, String name) {
        abstractRenderTask.setName(name);
        renderTasks.add(abstractRenderTask);
    }

    public void removeRenderTask(AbstractRenderTask abstractRenderTask) {
        renderTasks.remove(abstractRenderTask);
    }

    public boolean containsRenderTask(AbstractRenderTask abstractRenderTask) {
        return renderTasks.contains(abstractRenderTask);
    }

    public void render() {
        try {
            perfmonService.onEntered(PerfmonEnum.RENDERER);
            long timeStamp = System.currentTimeMillis();
            renderTasks.forEach(renderTask -> renderTask.prepareRender(timeStamp));
            prepareDepthBufferRendering();
            renderTasks.forEach(AbstractRenderTask::drawDepthBuffer);
            prepareMainRendering();

            for (RenderUnitControl renderUnitControl : RenderUnitControl.getRenderUnitControls()) {
                prepare(renderUnitControl);
                renderTasks.forEach(abstractRenderTask -> abstractRenderTask.draw(renderUnitControl));
            }
            prepare(RenderUnitControl.NORMAL);

            if (showNorm) {
                // depthTest(false);
                renderTasks.forEach(AbstractRenderTask::drawNorm);
                // depthTest(true);
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        } finally {
            perfmonService.onLeft(PerfmonEnum.RENDERER);
        }
    }


    public void fillBuffers() {
        for (AbstractRenderTask renderTask : renderTasks) {
            try {
                renderTask.fillBuffers();
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
    }

    public int getRenderQueueSize() {
        int i = 0;
        for (AbstractRenderTask renderTask : renderTasks) {
            i += renderTask.getAll().size();
        }
        return i;
    }

    public boolean isShowNorm() {
        return showNorm;
    }

    public void setShowNorm(boolean showNorm) {
        this.showNorm = showNorm;
        if (showNorm) {
            for (AbstractRenderTask compositeRenderer : renderTasks) {
                try {
                    compositeRenderer.fillNormBuffer();
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            }
        }
    }

    public List<AbstractRenderTask> getRenderTasks() {
        return renderTasks;
    }
}
