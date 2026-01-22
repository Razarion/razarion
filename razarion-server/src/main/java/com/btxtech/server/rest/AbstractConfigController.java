package com.btxtech.server.rest;

import com.btxtech.server.service.engine.AbstractConfigCrudService;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public abstract class AbstractConfigController<C extends Config> {
    protected abstract AbstractConfigCrudService<C, ?> getConfigCrudService();

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(value = "objectNameIds", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public List<ObjectNameId> getObjectNameIds() {
        return getConfigCrudService().getObjectNameIds();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(value = "objectNameId/{id}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ObjectNameId getObjectNameId(@PathVariable("id") int id) {
        return getConfigCrudService().getObjectNameId(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "create", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public C create() {
        return getConfigCrudService().create();
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping(value = "delete/{id}")
    public void delete(@PathVariable("id") int id) {
        getConfigCrudService().delete(id);
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    @PostMapping(value = "update", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public void update(@RequestBody C config) {
        getConfigCrudService().update(config);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(value = "read/{id}", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public C read(@PathVariable("id") int id) {
        return getConfigCrudService().read(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(value = "read", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public List<C> readAll() {
        return getConfigCrudService().read();
    }
}
