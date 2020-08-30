package com.btxtech.uiservice.renderer;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;
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
    private Pass pass;

    protected abstract void internalSetup();

    protected abstract void prepareMainRendering();

    protected abstract void prepareDepthBufferRendering();

    protected abstract void prepare(RenderUnitControl renderUnitControl);

    public void setup() {
        internalSetup();
        renderTasks.clear();

        addRenderTask(GroundRenderTask.class, "Ground");
// TODO        addRenderTask(SlopeRenderTask.class, "Slope");
// TODO        addRenderTask(TerrainObjectRenderTask.class, "Terrain Object");
// TODO       addRenderTask(ItemMarkerRenderTask.class, "Item Marker");
// TODO       addRenderTask(BaseItemRenderTask.class, "Base Item");
// TODO       addRenderTask(TrailRenderTask.class, "Trail");
// TODO       addRenderTask(ResourceItemRenderTask.class, "Resource");
// TODO       addRenderTask(BoxItemRenderTask.class, "Box");
        addRenderTask(WaterRenderTask.class, "Water");
// TODO       addRenderTask(ProjectileRenderTask.class, "Projectile");
// TODO       addRenderTask(BaseItemPlacerRenderTask.class, "Base Item Placer");
// TODO       addRenderTask(SelectionFrameRenderTask.class, "Selection Frame");
// TODO       addRenderTask(ItemVisualizationRenderTask.class, "Tip");
// TODO       addRenderTask(ParticleRenderTask.class, "Particle");
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

            pass = Pass.SHADOW;
            prepareDepthBufferRendering();
            for (RenderUnitControl renderUnitControl : RenderUnitControl.getRenderUnitControls()) {
                // prepare(renderUnitControl);
                renderTasks.forEach(abstractRenderTask -> abstractRenderTask.draw(renderUnitControl));
            }

            pass = Pass.MAIN;
            prepareMainRendering();

            for (RenderUnitControl renderUnitControl : RenderUnitControl.getRenderUnitControls()) {
                prepare(renderUnitControl);
                renderTasks.forEach(abstractRenderTask -> abstractRenderTask.draw(renderUnitControl));
            }

            pass = null;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        } finally {
            perfmonService.onLeft(PerfmonEnum.RENDERER);
        }
    }


    public int getRenderQueueSize() {
        int i = 0;
        for (AbstractRenderTask renderTask : renderTasks) {
            i += renderTask.getAll().size();
        }
        return i;
    }

    public Pass getPass() {
        return pass;
    }

    public List<AbstractRenderTask> getRenderTasks() {
        return renderTasks;
    }

    public enum Pass {
        SHADOW,
        MAIN
    }
}
