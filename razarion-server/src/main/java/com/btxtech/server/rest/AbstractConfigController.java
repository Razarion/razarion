package com.btxtech.server.rest;

import com.btxtech.server.model.Roles;
import com.btxtech.server.service.engine.AbstractConfigCrudService;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public abstract class AbstractConfigController<C extends Config> {
    protected abstract AbstractConfigCrudService<C, ?> getConfigCrudService();

    @RolesAllowed(Roles.ADMIN)
    @GetMapping(value = "objectNameIds", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public List<ObjectNameId> getObjectNameIds() {
        return getConfigCrudService().getObjectNameIds();
    }

    @RolesAllowed(Roles.ADMIN)
    @GetMapping(value = "objectNameId/{id}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ObjectNameId getObjectNameId(@PathVariable("id") int id) {
        return getConfigCrudService().getObjectNameId(id);
    }

    @RolesAllowed(Roles.ADMIN)
    @PostMapping(value = "create", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public C create() {
        return getConfigCrudService().create();
    }


    @RolesAllowed(Roles.ADMIN)
    @DeleteMapping(value = "delete/{id}")
    public void delete(@PathVariable("id") int id) {
        getConfigCrudService().delete(id);
    }


    @RolesAllowed(Roles.ADMIN)
    @Transactional
    @PostMapping(value = "update", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public void update(@RequestBody C config) {
        getConfigCrudService().update(config);
    }

    @RolesAllowed(Roles.ADMIN)
    @GetMapping(value = "read/{id}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public C read(@PathVariable("id") int id) {
        return getConfigCrudService().read(id);
    }

    @RolesAllowed(Roles.ADMIN)
    @GetMapping(value = "read", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public List<C> readAll() {
        return getConfigCrudService().read();
    }
}
