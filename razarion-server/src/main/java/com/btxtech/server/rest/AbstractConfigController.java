package com.btxtech.server.rest;

import com.btxtech.server.service.engine.AbstractConfigCrudPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public abstract class AbstractConfigController<C extends Config> {
    protected abstract AbstractConfigCrudPersistence<C, ?> getConfigCrudPersistence();

    @SecurityCheck
    @GetMapping(value = "objectNameIds", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public List<ObjectNameId> getObjectNameIds() {
        return getConfigCrudPersistence().getObjectNameIds();
    }

    @SecurityCheck
    @GetMapping(value = "objectNameId/{id}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ObjectNameId getObjectNameId(@PathVariable("id") int id) {
        return getConfigCrudPersistence().getObjectNameId(id);
    }

    @SecurityCheck
    @PostMapping(value = "create", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public C create() {
        return getConfigCrudPersistence().create();
    }


    @SecurityCheck
    @DeleteMapping(value = "delete/{id}")
    public void delete(@PathVariable("id") int id) {
        getConfigCrudPersistence().delete(id);
    }


    @SecurityCheck
    @Transactional
    @PostMapping(value = "update", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public void update(@RequestBody C config) {
        getConfigCrudPersistence().update(config);
    }

    @SecurityCheck
    @GetMapping(value = "read/{id}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public C read(@PathVariable("id") int id) {
        return getConfigCrudPersistence().read(id);
    }

    @SecurityCheck
    @GetMapping(value = "read", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public List<C> readAll() {
        return getConfigCrudPersistence().read();
    }
}
