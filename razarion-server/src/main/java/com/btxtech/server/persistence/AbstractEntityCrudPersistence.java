package com.btxtech.server.persistence;

import com.btxtech.shared.dto.ObjectNameId;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractEntityCrudPersistence<E> {
    @PersistenceContext
    private EntityManager entityManager;
    private final Class<E> entityClass;
    private final SingularAttribute<E, Integer> id;
    private final SingularAttribute<E, String> internalName;

    public AbstractEntityCrudPersistence(Class<E> entityClass, SingularAttribute<E, Integer> id, SingularAttribute<E, String> internalName) {
        this.entityClass = entityClass;
        this.id = id;
        this.internalName = internalName;
    }

    public <B extends BaseEntity> AbstractEntityCrudPersistence(Class<B> entityClass) {
        this.entityClass = (Class<E>) entityClass;
        this.id = (SingularAttribute<E, Integer>) BaseEntity_.id;
        this.internalName = (SingularAttribute<E, String>) BaseEntity_.internalName;
    }

    @Transactional
    public List<ObjectNameId> getObjectNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<E> root = cq.from(entityClass);
        cq.multiselect(root.get(id), root.get(internalName));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        return tupleResult.stream().map(t -> new ObjectNameId(((int) t.get(0)), (String) t.get(1))).collect(Collectors.toList());
    }

    @Transactional
    public void delete(int id) {
        E entity = entityManager.find(entityClass, id);
        if (entity == null) {
            throw new IllegalArgumentException("No entry for id: " + id);
        }
        entityManager.remove(entity);
    }

    protected E newEntity() {
        try {
            return entityClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public E getEntity(Integer id) {
        if (id == null) {
            return null;
        }
        E entity = entityManager.find(entityClass, id);
        if (entity == null) {
            throw new IllegalArgumentException("No " + entityClass + " for id: " + id);
        }
        return entity;
    }

    public List<E> getEntities() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> userQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> root = userQuery.from(entityClass);
        CriteriaQuery<E> userSelect = userQuery.select(root);
        return entityManager.createQuery(userSelect).getResultList();
    }

    @Transactional
    public E createBaseEntity() {
        E e = newEntity();
        entityManager.persist(e);
        return e;
    }

    @Transactional
    public E getBaseEntity(Integer id) {
        return getEntity(id);
    }

    @Transactional
    public void updateBaseEntity(E entity) {
        entityManager.merge(entity);
    }

    @Transactional
    public List<E> readAllBaseEntities() {
        return getEntities();
    }
}
