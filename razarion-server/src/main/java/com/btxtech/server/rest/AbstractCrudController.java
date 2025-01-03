package com.btxtech.server.rest;

import com.btxtech.server.persistence.AbstractConfigCrudPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.rest.CrudController;

import java.util.List;

public abstract class AbstractCrudController<C extends Config, E> implements CrudController<C> {
    @SecurityCheck
    @Override
    public List<ObjectNameId> getObjectNameIds() {
        return getCrudPersistence().getObjectNameIds();
    }

    @Override
    public ObjectNameId getObjectNameId(int id) {
        return getCrudPersistence().read(id).createObjectNameId();
    }

    @SecurityCheck
    @Override
    public C create() {
        return getCrudPersistence().create();
    }

    @SecurityCheck
    @Override
    public void delete(int id) {
        getCrudPersistence().delete(id);
    }

    @SecurityCheck
    @Override
    public void update(C config) {
        getCrudPersistence().update(config);
    }

    @SecurityCheck
    @Override
    public C read(int id) {
        return getCrudPersistence().read(id);
    }

    @SecurityCheck
    @Override
    public List<C> readAll() {
        return getCrudPersistence().read();
    }

    protected abstract AbstractConfigCrudPersistence<C, E> getCrudPersistence();
}
