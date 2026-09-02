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
        // The same lookup, and the same answer when there is no row: deleting something that is
        // not there is a 404, not a fault.
        jpaRepository.findById(id).orElseThrow(() -> new NoSuchEntityException(entityClass, id));
        jpaRepository.deleteById(id);
    }

    protected E newEntity() {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The entity with this id, or null if no id was given.
     * <p>
     * A null id is not a mistake here and does not throw: this is how an optional foreign key is
     * mapped, and {@code getGroundBabylonMaterialId()} returning null means "no material set", not
     * "material missing". A REST path cannot reach that branch anyway - the id arrives as a
     * primitive path variable.
     * <p>
     * An id that is given and has no row is a different thing, and now says so. It used to end in
     * a bare {@code orElseThrow()}, which answered every such request with a 500 and a stack trace
     * indistinguishable from a real fault.
     */
    public E getEntity(Integer id) {
        if (id == null) {
            return null;
        }
        return jpaRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchEntityException(entityClass, id));
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
