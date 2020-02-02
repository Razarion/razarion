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
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @param <C> Config
 * @param <E> Entity
 */
public abstract class CrudPersistence<C, E> {
    @PersistenceContext
    private EntityManager entityManager;
    private Class<E> entityClass;
    private SingularAttribute<E, Integer> id;
    private SingularAttribute<E, String> internalName;
    private Function<C, Integer> configIdProvider;

    public CrudPersistence(Class<E> entityClass, SingularAttribute<E, Integer> id, SingularAttribute<E, String> internalName, Function<C, Integer> configIdProvider) {
        this.entityClass = entityClass;
        this.id = id;
        this.internalName = internalName;
        this.configIdProvider = configIdProvider;
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
    public C create() {
        E entity = newEntity();
        entityManager.persist(entity);
        return toConfig(entity);
    }

    @Transactional
    public void delete(int id) {
        E entity = entityManager.find(entityClass, id);
        entityManager.remove(entity);
    }

    @Transactional
    public void update(C config) {
        E entity = entityManager.find(entityClass, configIdProvider.apply(config));
        fromConfig(config, entity);
        entityManager.merge(entity);
    }

    @Transactional
    public C read(int id) {
        return toConfig(entityManager.find(entityClass, id));
    }

    @Transactional
    public List<C> read() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> userQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> root = userQuery.from(entityClass);
        CriteriaQuery<E> userSelect = userQuery.select(root);
        Collection<E> slopeConfigEntities = entityManager.createQuery(userSelect).getResultList();

        return slopeConfigEntities.stream().map(this::toConfig).collect(Collectors.toList());
    }

    protected abstract C toConfig(E entity);

    protected abstract void fromConfig(C entity, E config);

    protected E newEntity() {
        try {
            return entityClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
