package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.PARTICLE_EMITTER_SEQUENCE_EDITOR_PATH)
public interface ParticleEmitterSequenceEditorController extends CrudController<ParticleEmitterSequenceConfig> {
}
