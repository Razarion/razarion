package com.btxtech.server.persistence;

import com.btxtech.shared.dto.Config;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.SingularAttribute;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @param <C> Config
 * @param <E> Entity
 */
public abstract class AbstractConfigCrudPersistence<C extends Config, E> extends AbstractEntityCrudPersistence<E> {
    @PersistenceContext
    private EntityManager entityManager;

    public AbstractConfigCrudPersistence(Class<E> entityClass, SingularAttribute<E, Integer> id, SingularAttribute<E, String> internalName) {
        super(entityClass, id, internalName);
    }

    @Transactional
    public C create() {
        try {
            E entity = newEntity();
            C config = newConfig();
            if (config != null) {
                fromConfig(config, entity);
            }
            entityManager.persist(entity);
            return toConfig(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional
    public void update(C config) {
        E entity = getEntity(config.getId());
        fromConfig(config, entity);
        entityManager.merge(entity);
    }

    @Transactional
    public C read(int id) {
        return toConfig(getEntity(id));
    }

    @Transactional
    public List<C> read() {
        return getEntities()
                .stream()
                .map(this::toConfig)
                .collect(Collectors.toList());
    }

    protected abstract C toConfig(E entity);

    protected abstract void fromConfig(C config, E entity);

    protected C newConfig() {
        return null;
    }

}
