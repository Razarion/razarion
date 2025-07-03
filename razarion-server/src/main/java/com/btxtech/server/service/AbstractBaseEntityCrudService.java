package com.btxtech.server.service;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.dto.ObjectNameId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractBaseEntityCrudService<E extends BaseEntity> {
    private final Class<E> entityClass;
    private final JpaRepository<E, Integer> jpaRepository;

    public AbstractBaseEntityCrudService(Class<E> entityClass, JpaRepository<E, Integer> jpaRepository) {
        this.entityClass = entityClass;
        this.jpaRepository = jpaRepository;
    }

    public final JpaRepository<E, Integer> getJpaRepository() {
        return jpaRepository;
    }

    @Transactional
    public List<ObjectNameId> getObjectNameIds() {
        return jpaRepository
                .findAll()
                .stream()
                .map(baseEntity -> new ObjectNameId(baseEntity.getId(), baseEntity.getInternalName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(int id) {
        jpaRepository.findById(id).orElseThrow();
        jpaRepository.deleteById(id);
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
        return jpaRepository
                .findById(id)
                .orElseThrow();
    }

    public List<E> getEntities() {
        return new ArrayList<>(jpaRepository.findAll());
    }

    @Transactional
    public E createBaseEntity() {
        E e = newEntity();
        jpaRepository.save(e);
        return e;
    }

    @Transactional
    public E getBaseEntity(Integer id) {
        return getEntity(id);
    }

    @Transactional
    public void updateBaseEntity(E entity) {
        entity = jsonToJpa(entity);
        jpaRepository.save(entity);
    }

    @Transactional
    public List<E> readAllBaseEntities() {
        return getEntities();
    }

    protected E jsonToJpa(E entity) {
        return entity;
    }

}
