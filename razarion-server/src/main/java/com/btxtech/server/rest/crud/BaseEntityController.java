package com.btxtech.server.rest.crud;

import com.btxtech.server.persistence.AbstractEntityCrudPersistence;
import com.btxtech.server.persistence.BaseEntity;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.ObjectNameId;

import javax.transaction.Transactional;
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
