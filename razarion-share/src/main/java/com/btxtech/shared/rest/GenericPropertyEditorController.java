package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path(CommonUrl.GENERIC_PROPERTY_EDITOR_PATH)
public interface GenericPropertyEditorController {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("getlisttypearguments")
    Map<String, Map<String, String>> getListTypeArguments();
}
