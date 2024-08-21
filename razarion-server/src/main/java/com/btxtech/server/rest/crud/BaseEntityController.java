package com.btxtech.server.rest.crud;

import com.btxtech.server.persistence.AbstractEntityCrudPersistence;
import com.btxtech.server.persistence.BaseEntity;
import com.btxtech.server.user.SecurityCheck;
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

public abstract class BaseEntityController<E extends BaseEntity> {

    protected abstract AbstractEntityCrudPersistence<E> getEntityCrudPersistence();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("objectNameIds")
    @SecurityCheck
    public List<ObjectNameId> getObjectNameIds() {
        return getEntityCrudPersistence().getObjectNameIds();
    }

    @POST
    @Path("create")
    @Produces(MediaType.APPLICATION_JSON)
    public E create() {
        return getEntityCrudPersistence().createBaseEntity();
    }

    @DELETE
    @Path("delete/{id}")
    public void delete(@PathParam("id") int id) {
        getEntityCrudPersistence().delete(id);
    }

    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(E entity) {
        getEntityCrudPersistence().updateBaseEntity(entity);
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("read/{id}")
    public E read(@PathParam("id") int id) {
        return getEntityCrudPersistence().getBaseEntity(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("read")
    public List<E> readAll() {
        return getEntityCrudPersistence().readAllBaseEntities();
    }
}
