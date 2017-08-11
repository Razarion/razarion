package com.btxtech.server.persistence.server;

import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 03.08.2017.
 * <p>
 * R = root
 * P = parent
 * E = entity
 * C = config
 */
public class ServerChildListCrudePersistence<R, P, E extends ObjectNameIdProvider, C> {
    @PersistenceContext
    private EntityManager entityManager;
    private Supplier<R> rootProvider;
    private Function<EntityManager, P> parentProvider;
    private Function<P, List<E>> entitiesGetter;
    private BiConsumer<P, List<E>> entitiesSetter;
    private Function<E, Integer> entityIdProvider;
    private Function<C, Integer> configIdProvider;
    private Supplier<E> entityFactory;
    private BiConsumer<E, C> entityFiller;
    private Function<E, C> configGenerator;
    private BiConsumer<EntityManager, Integer> additionalDelete;

    public ServerChildListCrudePersistence<R, P, E, C> setRootProvider(Supplier<R> rootProvider) {
        this.rootProvider = rootProvider;
        return this;
    }

    public ServerChildListCrudePersistence<R, P, E, C> setParentProvider(Function<EntityManager, P> parentProvider) {
        this.parentProvider = parentProvider;
        return this;
    }

    public ServerChildListCrudePersistence<R, P, E, C> setEntitiesGetter(Function<P, List<E>> entitiesGetter) {
        this.entitiesGetter = entitiesGetter;
        return this;
    }

    public ServerChildListCrudePersistence<R, P, E, C> setEntitiesSetter(BiConsumer<P, List<E>> entitiesSetter) {
        this.entitiesSetter = entitiesSetter;
        return this;
    }

    public ServerChildListCrudePersistence<R, P, E, C> setEntityIdProvider(Function<E, Integer> entityIdProvider) {
        this.entityIdProvider = entityIdProvider;
        return this;
    }

    public ServerChildListCrudePersistence<R, P, E, C> setConfigIdProvider(Function<C, Integer> configIdProvider) {
        this.configIdProvider = configIdProvider;
        return this;
    }

    public ServerChildListCrudePersistence<R, P, E, C> setEntityFactory(Supplier<E> entityFactory) {
        this.entityFactory = entityFactory;
        return this;
    }

    public ServerChildListCrudePersistence<R, P, E, C> setEntityFiller(BiConsumer<E, C> entityFiller) {
        this.entityFiller = entityFiller;
        return this;
    }

    public ServerChildListCrudePersistence<R, P, E, C> setConfigGenerator(Function<E, C> configGenerator) {
        this.configGenerator = configGenerator;
        return this;
    }

    public ServerChildListCrudePersistence setAdditionalDelete(BiConsumer<EntityManager, Integer> additionalDelete) {
        this.additionalDelete = additionalDelete;
        return this;
    }

    @Transactional
    @SecurityCheck
    public List<ObjectNameId> readObjectNameIds() {
        P p = parentProvider.apply(entityManager);
        if (p == null) {
            throw new IllegalStateException("No parent");
        }
        List<E> entities = entitiesGetter.apply(p);
        if (entities == null) {
            return new ArrayList<>();
        } else {
            return entities.stream().map(ObjectNameIdProvider::createObjectNameId).collect(Collectors.toList());
        }
    }

    @Transactional
    @SecurityCheck
    public C read(int id) {
        P p = parentProvider.apply(entityManager);
        if (p == null) {
            throw new IllegalStateException("No parent");
        }
        List<E> entities = entitiesGetter.apply(p);
        return entities.stream().filter(e -> id == entityIdProvider.apply(e)).findFirst().map(e -> configGenerator.apply(e)).orElseThrow(() -> new IllegalArgumentException("No entity for id: " + id));
    }

    @Transactional
    @SecurityCheck
    public C create() {
        P p = parentProvider.apply(entityManager);
        if (p == null) {
            throw new IllegalStateException("No parent");
        }
        List<E> entities = entitiesGetter.apply(p);
        if (entities == null) {
            entities = new ArrayList<>();
        }
        E entity = entityFactory.get();
        entities.add(entity);
        entitiesSetter.accept(p, entities);
        entityManager.persist(rootProvider.get()); // Ignores changes on parent but child id is set
        return configGenerator.apply(entity);
    }

    @Transactional
    @SecurityCheck
    public void update(C config) {
        P p = parentProvider.apply(entityManager);
        if (p == null) {
            throw new IllegalStateException("No parent");
        }
        List<E> entities = entitiesGetter.apply(p);
        if (entities != null) {
            E entity = entities.stream().filter(e -> configIdProvider.apply(config).equals(entityIdProvider.apply(e))).findFirst().orElseThrow(() -> new IllegalArgumentException("No Entity for id: " + configIdProvider.apply(config)));
            entityFiller.accept(entity, config);
        } else {
            throw new IllegalArgumentException("No Entity for id: " + configIdProvider.apply(config));
        }
        entityManager.merge(rootProvider.get());
    }

    @Transactional
    @SecurityCheck
    public void delete(int id) {
        P p = parentProvider.apply(entityManager);
        if (p == null) {
            throw new IllegalStateException("No parent");
        }
        List<E> entities = entitiesGetter.apply(p);
        if (entities != null) {
            entities.removeIf(e -> id == entityIdProvider.apply(e));
            entityManager.merge(rootProvider.get());
            if (additionalDelete != null) {
                additionalDelete.accept(entityManager, id);
            }
        }
    }

    @Transactional
    @SecurityCheck
    public void swap(int index1, int index2) {
        P p = parentProvider.apply(entityManager);
        if (p == null) {
            throw new IllegalStateException("No parent");
        }
        List<E> entities = entitiesGetter.apply(p);
        Collections.swap(entities, index1, index2);
        entityManager.merge(rootProvider.get());
    }
}
