package com.btxtech.server.rest;

import com.btxtech.server.persistence.DbPropertyConfig;
import com.btxtech.server.persistence.DbPropertiesService;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.CommonUrl;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path(CommonUrl.PROPERTIES_EDITOR_CONTROLLER)
public class DbPropertiesEditorController {
    @Inject
    private DbPropertiesService dbPropertiesService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("get-all")
    @SecurityCheck
    public List<DbPropertyConfig> readAllProperties() {
        return dbPropertiesService.getDbPropertyConfigs();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("update")
    @SecurityCheck
    public void updateProperty(DbPropertyConfig dbPropertyConfig) {
        dbPropertiesService.saveDbPropertyConfig(dbPropertyConfig);
    }

}
