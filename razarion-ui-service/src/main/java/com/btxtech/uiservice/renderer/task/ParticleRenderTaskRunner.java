package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.particle.ParticleShapeConfig;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;
import com.btxtech.uiservice.renderer.WebGlRenderTask;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_PARTICLE_SHAPE_CONFIG;

/**
 * Created by Beat
 * 01.02.2017.
 */
@ApplicationScoped
public class ParticleRenderTaskRunner extends AbstractRenderTaskRunner {
    public interface RenderTask extends WebGlRenderTask<ParticleShapeConfig> {
    }

    // private Logger logger = Logger.getLogger(ParticleRenderTask.class.getName());
    @Inject
    private AlarmService alarmService;
    @Inject
    private ParticleService particleService;

    @PostConstruct
    public void postConstruct() {
        particleService.getParticleShapeConfigs().forEach(this::setupParticleConfig);
    }

    public void editorReload() {
        destroyRenderAllTasks();
        postConstruct();
    }

    @Override
    protected void preRender(long timeStamp) {
        particleService.preRender(timeStamp);
    }

    private void setupParticleConfig(ParticleShapeConfig particleShapeConfig) {
        if (particleShapeConfig.getAlphaOffsetImageId() == null) {
            alarmService.riseAlarm(INVALID_PARTICLE_SHAPE_CONFIG, "no alphaOffsetImageId", particleShapeConfig.getId());
            return;
        }
        if (particleShapeConfig.getColorRampImageId() == null) {
            alarmService.riseAlarm(INVALID_PARTICLE_SHAPE_CONFIG, "no colorRampImageId", particleShapeConfig.getId());
            return;
        }

        createModelRenderTask(RenderTask.class,
                particleShapeConfig,
                timeStamp -> particleService.provideModelMatrices(particleShapeConfig.getId()),
                null,
                null,
                null);
    }
}
