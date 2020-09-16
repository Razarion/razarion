package com.btxtech.uiservice.renderer.task.particle;

import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.particle.ParticleShapeConfig;
import com.btxtech.uiservice.renderer.AbstractModelRenderTaskRunner;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 01.02.2017.
 */
@ApplicationScoped
public class ParticleRenderTask extends AbstractModelRenderTaskRunner<Void> {
    // private Logger logger = Logger.getLogger(ParticleRenderTask.class.getName());
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
        ModelRenderer<Void> modelRenderer = create();
        modelRenderer.init(null, timeStamp -> particleService.provideModelMatrices());
        CommonRenderComposite<AbstractParticleRenderUnit, ParticleShapeConfig> compositeRenderer = modelRenderer.create();
        compositeRenderer.init(particleShapeConfig);
        compositeRenderer.setRenderUnit(AbstractParticleRenderUnit.class);
        compositeRenderer.setDepthBufferRenderUnit(AbstractParticleRenderUnit.class);
        modelRenderer.add(RenderUnitControl.PARTICLE, compositeRenderer);
        add(modelRenderer);
        compositeRenderer.fillBuffers();
    }
}
