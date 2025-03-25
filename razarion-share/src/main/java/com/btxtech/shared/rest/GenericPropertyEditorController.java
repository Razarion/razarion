package com.btxtech.shared.rest;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.editor.GenericPropertyInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(CommonUrl.GENERIC_PROPERTY_EDITOR_PATH)
public interface GenericPropertyEditorController {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get-generic-property-info")
    GenericPropertyInfo getGenericPropertyInfo();
}
