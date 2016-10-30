package com.btxtech.uiservice.renderer;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.renderer.task.BaseItemRenderTask;
import com.btxtech.uiservice.renderer.task.BoxItemRenderTask;
import com.btxtech.uiservice.renderer.task.ClipRenderTask;
import com.btxtech.uiservice.renderer.task.ProjectileRenderTask;
import com.btxtech.uiservice.renderer.task.ResourceItemRenderTask;
import com.btxtech.uiservice.renderer.task.TerrainObjectRenderTask;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;
import com.btxtech.uiservice.renderer.task.selection.SelectionFrameRenderTask;
import com.btxtech.uiservice.renderer.task.slope.SlopeRenderTask;
import com.btxtech.uiservice.renderer.task.startpoint.StartPointUiService;
import com.btxtech.uiservice.renderer.task.water.WaterRenderTask;

import javax.enterprise.event.Event;
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
    private Event<RenderServiceInitEvent> serviceInitEvent;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<AbstractRenderTask> instance;
    private List<AbstractRenderTask> renderTasks = new ArrayList<>();
    private boolean showNorm;

    protected abstract void prepareMainRendering();

    protected abstract void prepareDepthBufferRendering();

    protected abstract void prepare(RenderUnitControl renderUnitControl);

    public void setup() {
        serviceInitEvent.fire(new RenderServiceInitEvent());
        renderTasks.clear();

        addRenderTask(GroundRenderTask.class);
//        addRenderTask(SlopeRenderTask.class);
//        addRenderTask(TerrainObjectRenderTask.class);
        addRenderTask(BaseItemRenderTask.class);
//        addRenderTask(ResourceItemRenderTask.class);
        addRenderTask(BoxItemRenderTask.class);
//        addRenderTask(WaterRenderTask.class);
//        addRenderTask(ProjectileRenderTask.class);
//        addRenderTask(ClipRenderTask.class);
        addRenderTask(StartPointUiService.class);
        addRenderTask(SelectionFrameRenderTask.class);

        fillBuffers();
    }

    private void addRenderTask(Class<? extends AbstractRenderTask> clazz) {
        renderTasks.add(instance.select(clazz).get());
    }

    public void render() {
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
}
