package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.particle.ParticleShapeConfig;

import javax.ws.rs.Path;

@Path(CommonUrl.PARTICLE_SHAPE_EDITOR_PATH)
public interface ParticleShapeEditorController extends CrudController<ParticleShapeConfig> {
}
