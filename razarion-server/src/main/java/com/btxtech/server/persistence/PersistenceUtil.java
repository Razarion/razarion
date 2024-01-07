package com.btxtech.server.persistence;

import com.btxtech.server.persistence.itemtype.BaseItemTypeEntity;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 21.10.2016.
 */
public interface PersistenceUtil {
    static <E> Integer extractId(E e, Function<E, Integer> idSupplier) {
        if (e != null) {
            return idSupplier.apply(e);
        }
        return null;
    }

    static <E, C> E fromConfig(E outputEntity, C inputConfig, Supplier<E> entityCreator, BiConsumer<E, C> entityFiller) {
        if (inputConfig == null) {
            return null;
        }
        if (outputEntity == null) {
            outputEntity = entityCreator.get();
        }
        entityFiller.accept(outputEntity, inputConfig);

        return outputEntity;
    }

    static <T> List<T> readAllEntities(EntityManager entityManager, Class<T> theClass, SingularAttribute<T, Date> orderByAttribute) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> userQuery = criteriaBuilder.createQuery(theClass);
        Root<T> from = userQuery.from(theClass);
        CriteriaQuery<T> userSelect = userQuery.select(from);
        if (orderByAttribute != null) {
            userQuery.orderBy(criteriaBuilder.desc(from.join(orderByAttribute)));
        }
        return entityManager.createQuery(userSelect).getResultList();
    }

    /**
     * E : entity
     * C : config
     */
    static <E, C> List<E> toChildEntityList(List<E> entityList, List<C> configList, Supplier<E> entityGenerator, Function<E, Object> entityIdProvider, BiConsumer<E, C> entityFiller, Function<C, Object> configIdProvider) {
        List<E> resultEntityList = new ArrayList<>();
        if (configList != null) {
            for (C config : configList) {
                E entity = null;
                if (configIdProvider.apply(config) != null) {
                    if (entityList != null) {
                        entity = entityList.stream().filter(e -> entityIdProvider.apply(e).equals(configIdProvider.apply(config))).findFirst().orElse(null);
                    }
                }
                if (entity == null) {
                    entity = entityGenerator.get();
                }
                entityFiller.accept(entity, config);
                resultEntityList.add(entity);
            }
        }
        if (entityList == null) {
            entityList = new ArrayList<>();
        }
        entityList.clear();
        entityList.addAll(resultEntityList);
        return entityList;
    }

    static Map<Integer, Integer> extractItemTypeLimitation(Map<BaseItemTypeEntity, Integer> itemTypeLimitation) {
        if (itemTypeLimitation != null) {
            return itemTypeLimitation.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue, (a, b) -> b));
        }
        return Collections.emptyMap();
    }

    static List<Double> extractList(List<Double> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(list);
    }

    static List<Double> toList(List<Double> output, List<Double> input) {
        if (output == null) {
            output = new ArrayList<>();
        }
        output.clear();
        if (input != null) {
            output.addAll(input);
        }
        return output;
    }

    static <C, E> C toConfig(E entity, Function<E, C> configProvider) {
        if (entity == null) {
            return null;
        }
        return configProvider.apply(entity);
    }

    static <C, E> List<C> toConfigList(List<E> entities, Function<E, C> configProvider) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(configProvider).collect(Collectors.toList());
    }

    static <C, E> List<E> fromConfigs(List<E> outputEntities, List<C> inputConfigs, Supplier<E> entityCreator, BiConsumer<E, C> entityFiller) {
        if (outputEntities == null) {
            outputEntities = new ArrayList<>();
        }
        outputEntities.clear();
        if (inputConfigs != null) {
            for (C config : inputConfigs) {
                E entity = entityCreator.get();
                entityFiller.accept(entity, config);
                outputEntities.add(entity);
            }
        }
        return outputEntities;
    }

    static <C, E> List<E> fromConfigsNoClear(List<E> outputEntities, List<C> inputConfigs, Supplier<E> entityCreator, BiConsumer<E, C> entityFiller, Function<C, Integer> getConfigId, Function<E, Integer> getEntityId) {
        // To order of outputEntities is mixed up
        if (outputEntities == null) {
            outputEntities = new ArrayList<>();
        }
        if (inputConfigs == null) {
            inputConfigs = new ArrayList<>();
        }
        List<E> newEntities = new ArrayList<>();
        List<Integer> updatedEntityIds = new ArrayList<>();
        for (C config : inputConfigs) {
            E entity;
            Integer configId = getConfigId.apply(config);
            if(configId != null) {
                entity = outputEntities.stream().filter(e -> configId.equals(getEntityId.apply(e))).findFirst().orElse(null);
                if (entity == null) {
                    entity = entityCreator.get();
                    newEntities.add(entity);
                } else {
                    updatedEntityIds.add(getEntityId.apply(entity));
                }
            } else {
                entity = entityCreator.get();
                newEntities.add(entity);
            }
            entityFiller.accept(entity, config);
        }
        outputEntities.removeIf(e -> !updatedEntityIds.contains(getEntityId.apply(e)));
        outputEntities.addAll(newEntities);
        return outputEntities;
    }

    static <V> V defaultOnNull(V value, V defaultNullValue) {
        if (value == null) {
            return defaultNullValue;
        }
        return value;
    }

}
