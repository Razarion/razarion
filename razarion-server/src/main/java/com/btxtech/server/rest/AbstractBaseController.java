package com.btxtech.server.rest;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.Roles;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.shared.dto.ObjectNameId;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public abstract class AbstractBaseController<E extends BaseEntity> {

    protected abstract AbstractBaseEntityCrudService<E> getEntityCrudPersistence();

    @RolesAllowed(Roles.ADMIN)
    @GetMapping(value = "objectNameIds", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public List<ObjectNameId> getObjectNameIds() {
        return getEntityCrudPersistence().getObjectNameIds();
    }

    @RolesAllowed(Roles.ADMIN)
    @PostMapping(value = "create", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public E create() {
        return getEntityCrudPersistence().createBaseEntity();
    }

    @RolesAllowed(Roles.ADMIN)
    @DeleteMapping(value = "delete/{id}")
    public void delete(@PathVariable("id") int id) {
        getEntityCrudPersistence().delete(id);
    }

    @RolesAllowed(Roles.ADMIN)
    @Transactional
    @PostMapping(value = "update", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public void update(@RequestBody E entity) {
        getEntityCrudPersistence().updateBaseEntity(entity);
    }

    @RolesAllowed(Roles.ADMIN)
    @Transactional
    @GetMapping(value = "read/{id}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public E read(@PathVariable("id") int id) {
        return jpa2Json(getEntityCrudPersistence().getEntity(id));
    }

    @RolesAllowed(Roles.ADMIN)
    @Transactional
    @GetMapping(value = "read", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
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
