package com.btxtech.client.editor.particle;

import com.btxtech.client.editor.framework.AbstractCrudeEditor;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.uiservice.particle.ParticleService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 17.08.2016.
 */
@ApplicationScoped
public class ParticleCrud extends AbstractCrudeEditor<ParticleEmitterSequenceConfig> {
    // private Logger logger = Logger.getLogger(ParticleCrud.class.getName());
    @Inject
    private ParticleService particleService;

    @Override
    public void create() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reload() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getInstance(ObjectNameId objectNameId, Consumer<ParticleEmitterSequenceConfig> callback) {
        callback.accept(particleService.getParticleEmitterSequenceConfig(objectNameId.getId()));
    }

    @Override
    public void save(ParticleEmitterSequenceConfig particleEmitterSequenceConfig) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void delete(ParticleEmitterSequenceConfig particleEmitterSequenceConfig) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<ObjectNameId> setupObjectNameIds() {
        return particleService.getParticleEmitterSequenceConfigs().stream().map(ParticleEmitterSequenceConfig::createObjectNameId).collect(Collectors.toList());
    }

    @Override
    public void onChange(ParticleEmitterSequenceConfig particleEmitterSequenceConfig) {
        // TODO
    }
}
