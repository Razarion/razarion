package com.btxtech.server.rest;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.ObjectNameId;
import jakarta.transaction.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

public abstract class AbstractBaseController<E extends BaseEntity> {

    protected abstract AbstractBaseEntityCrudService<E> getEntityCrudPersistence();

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
    @SecurityCheck
    public E create() {
        return getEntityCrudPersistence().createBaseEntity();
    }

    @DELETE
    @Path("delete/{id}")
    @SecurityCheck
    public void delete(@PathParam("id") int id) {
        getEntityCrudPersistence().delete(id);
    }

    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    @SecurityCheck
    @Transactional
    public void update(E entity) {
        getEntityCrudPersistence().updateBaseEntity(entity);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("read/{id}")
    @SecurityCheck
    @Transactional
    public E read(@PathParam("id") int id) {
        return jpa2Json(getEntityCrudPersistence().getEntity(id));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("read")
    @SecurityCheck
    @Transactional
    public List<E> readAll() {
        return getEntityCrudPersistence().getEntities()
                .stream()
                .map(this::jpa2Json)
                .toList();
    }

    protected E jpa2Json(E entity) {
        return entity;
    }
}
