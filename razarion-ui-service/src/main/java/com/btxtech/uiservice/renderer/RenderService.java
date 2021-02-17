package com.btxtech.uiservice.renderer;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonEnum;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.uiservice.renderer.task.BaseItemPlacerRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.BaseItemRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.ParticleRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.ProjectileRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.ResourceItemRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.TerrainObjectRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.TrailRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.selection.ItemMarkerRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.selection.SelectionFrameRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.selection.StatusBarRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.simple.GroundRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.simple.SlopeRenderTaskRunner;
import com.btxtech.uiservice.renderer.task.simple.WaterRenderTaskRunner;

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
    private Instance<AbstractRenderTaskRunner> instance;
    @Inject
    private PerfmonService perfmonService;
    private List<AbstractRenderTaskRunner> renderTaskRunners = new ArrayList<>();
    private Pass pass;

    protected abstract void internalSetup();

    protected abstract void prepareMainRendering();

    protected abstract void prepareDepthBufferRendering();

    protected abstract void prepare();

    public void setup() {
        internalSetup();
        renderTaskRunners.clear();

        addRenderTaskRunner(GroundRenderTaskRunner.class, "Ground");
        addRenderTaskRunner(SlopeRenderTaskRunner.class, "Slope");
        addRenderTaskRunner(BaseItemPlacerRenderTaskRunner.class, "Base Item Placer");
        addRenderTaskRunner(TerrainObjectRenderTaskRunner.class, "Terrain Object");
        addRenderTaskRunner(ItemMarkerRenderTaskRunner.class, "Item Marker");
        addRenderTaskRunner(BaseItemRenderTaskRunner.class, "Base Item");
        addRenderTaskRunner(TrailRenderTaskRunner.class, "Trail");
        addRenderTaskRunner(ResourceItemRenderTaskRunner.class, "Resource");
// TODO       addRenderTaskRunner(BoxItemRenderTask.class, "Box");
        addRenderTaskRunner(WaterRenderTaskRunner.class, "Water");
        addRenderTaskRunner(StatusBarRenderTaskRunner.class, "Status Bar");
        addRenderTaskRunner(ProjectileRenderTaskRunner.class, "Projectile");
        addRenderTaskRunner(SelectionFrameRenderTaskRunner.class, "Selection Frame");
// TODO       addRenderTaskRunner(ItemVisualizationRenderTask.class, "Tip");
        addRenderTaskRunner(ParticleRenderTaskRunner.class, "Particle");
    }

    private void addRenderTaskRunner(Class<? extends AbstractRenderTaskRunner> clazz, String name) {
        addRenderTaskRunner(instance.select(clazz).get(), name);
    }

    public void addRenderTaskRunner(AbstractRenderTaskRunner abstractRenderTask, String name) {
        abstractRenderTask.setName(name);
        renderTaskRunners.add(abstractRenderTask);
    }

    public void removeRenderTaskRunner(AbstractRenderTaskRunner abstractRenderTask) {
        renderTaskRunners.remove(abstractRenderTask);
    }

    public void render() {
        try {
            perfmonService.onEntered(PerfmonEnum.RENDERER);
            long timeStamp = System.currentTimeMillis();

            pass = Pass.SHADOW;
            prepare();
            prepareDepthBufferRendering();
            renderTaskRunners.stream().filter(AbstractRenderTaskRunner::isEnabled).forEach(runner -> runner.draw(timeStamp));

            pass = Pass.MAIN;
            prepareMainRendering();
            renderTaskRunners.stream().filter(AbstractRenderTaskRunner::isEnabled).forEach(runner -> runner.draw(timeStamp));

            pass = null;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        } finally {
            perfmonService.onLeft(PerfmonEnum.RENDERER);
        }
    }

    public Pass getPass() {
        return pass;
    }

    public List<AbstractRenderTaskRunner> getRenderTaskRunners() {
        return renderTaskRunners;
    }

    public enum Pass {
        SHADOW,
        MAIN
    }
}
