package com.btxtech.shared.rest;

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

public interface CrudController<C extends Config> {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("objectNameIds")
    List<ObjectNameId> getObjectNameIds();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("objectNameId/{id}")
    ObjectNameId getObjectNameId(@PathParam("id") int id);

    @POST
    @Path("create")
    @Produces(MediaType.APPLICATION_JSON)
    C create();

    @DELETE
    @Path("delete/{id}")
    void delete(@PathParam("id") int id);

    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    void update(C config);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("read/{id}")
    C read(@PathParam("id") int id);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("read")
    List<C> readAll();

}
