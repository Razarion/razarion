package com.btxtech.server.service;

import com.btxtech.server.model.engine.BaseItemTypeEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PersistenceUtil {
    public static <E> Integer extractId(E e, Function<E, Integer> idSupplier) {
        if (e != null) {
            return idSupplier.apply(e);
        }
        return null;
    }

    public static <E, C> E fromConfig(E outputEntity, C inputConfig, Supplier<E> entityCreator, BiConsumer<E, C> entityFiller) {
        if (inputConfig == null) {
            return null;
        }
        if (outputEntity == null) {
            outputEntity = entityCreator.get();
        }
        entityFiller.accept(outputEntity, inputConfig);

        return outputEntity;
    }

    public static Map<Integer, Integer> extractItemTypeLimitation(Map<BaseItemTypeEntity, Integer> itemTypeLimitation) {
        if (itemTypeLimitation != null) {
            return itemTypeLimitation.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue, (a, b) -> b));
        }
        return Collections.emptyMap();
    }

    public static <C, E> List<C> toConfigList(List<E> entities, Function<E, C> configProvider) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(configProvider).collect(Collectors.toList());
    }

    public static <C, E> List<E> fromConfigsNoClear(List<E> outputEntities, List<C> inputConfigs, Supplier<E> entityCreator, BiConsumer<E, C> entityFiller, Function<C, Integer> getConfigId, Function<E, Integer> getEntityId) {
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

}
