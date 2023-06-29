package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.PARTICLE_SYSTEM_EDITOR_PATH)
public interface ParticleSystemEditorController extends CrudController<ParticleSystemConfig> {
}
