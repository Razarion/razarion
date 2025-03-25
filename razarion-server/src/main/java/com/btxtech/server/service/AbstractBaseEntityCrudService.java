package com.btxtech.server.service;

import com.btxtech.shared.dto.ObjectNameId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractBaseEntityCrudService<E extends BaseEntity> {
    private final Class<E> entityClass;

    @Deprecated
    public AbstractBaseEntityCrudService(Class<E> entityClass, Object id, Object internalName) {
        this.entityClass = entityClass;
    }

    public AbstractBaseEntityCrudService(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract JpaRepository<E, Integer> getJpaRepository();

    @Transactional
    public List<ObjectNameId> getObjectNameIds() {
        return getJpaRepository()
                .findAll()
                .stream()
                .map(baseEntity -> new ObjectNameId(baseEntity.getId(), baseEntity.getInternalName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(int id) {
        getJpaRepository().findById(id).orElseThrow();
        getJpaRepository().deleteById(id);
    }

    protected E newEntity() {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public E getEntity(Integer id) {
        if (id == null) {
            return null;
        }
        return getJpaRepository()
                .findById(id)
                .orElseThrow();
    }

    public List<E> getEntities() {
        return new ArrayList<>(getJpaRepository().findAll());
    }

    @Transactional
    public E createBaseEntity() {
        E e = newEntity();
        getJpaRepository().save(e);
        return e;
    }

    @Transactional
    public E getBaseEntity(Integer id) {
        return getEntity(id);
    }

    @Transactional
    public void updateBaseEntity(E entity) {
        getJpaRepository().save(entity);
    }

    @Transactional
    public List<E> readAllBaseEntities() {
        return getEntities();
    }

    protected E jsonToJpa(E entity) {
        return entity;
    }

}
