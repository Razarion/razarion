package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path(CommonUrl.THREE_JS_MODEL_PACK_EDITOR_PATH)
public interface ThreeJsModelPackEditorController extends CrudController<ThreeJsModelPackConfig> {

    @POST
    @Path("findByThreeJsModelId/{threeJsModelId}")
    @Produces(MediaType.APPLICATION_JSON)
    List<ThreeJsModelPackConfig> findByThreeJsModelId(@PathParam("threeJsModelId") int threeJsModelId);
}
