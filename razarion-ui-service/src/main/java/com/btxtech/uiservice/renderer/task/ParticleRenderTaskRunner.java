package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.particle.ParticleShapeConfig;
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
public class ParticleRenderTaskRunner extends AbstractShape3DRenderTaskRunner {
    public interface RenderTask extends WebGlRenderTask<ParticleShapeConfig> {
    }
    // private Logger logger = Logger.getLogger(ParticleRenderTask.class.getName());
    @Inject
    private AlarmService alarmService;
    @Inject
    private ParticleService particleService;

    @PostConstruct
    public void postConstruct() {
        setupParticleConfig(particleService.getParticleShapeConfig());
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

        WebGlRenderTask<ParticleShapeConfig> particleRenderTask = createModelRenderTask(RenderTask.class,
                particleShapeConfig,
                timeStamp -> particleService.provideModelMatrices(),
                null,
                null,
                null);
        particleRenderTask.setActive(true);
    }
}
