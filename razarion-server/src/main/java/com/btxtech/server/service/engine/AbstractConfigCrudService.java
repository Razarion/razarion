package com.btxtech.server.service.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.service.AbstractBaseEntityCrudService;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.ObjectNameId;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @param <C> Config
 * @param <E> Entity
 */
public abstract class AbstractConfigCrudService<C extends Config, E extends BaseEntity> extends AbstractBaseEntityCrudService<E> {
    @Autowired
    @Lazy
    private ServiceProviderService serviceProviderService;

    public AbstractConfigCrudService(Class<E> entityClass, JpaRepository<E, Integer> jpaRepository) {
        super(entityClass, jpaRepository);
    }

    @Transactional
    public C create() {
        try {
            E entity = newEntity();
            C config = newConfig();
            if (config != null) {
                fromConfig(config, entity);
            }
            getJpaRepository().save(entity);
            return toConfig(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Transactional
    public void update(C config) {
        E entity = getEntity(config.getId());
        fromConfig(config, entity);
        getJpaRepository().save(entity);
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

    public ObjectNameId getObjectNameId(int id) {
        E e = getEntity(id);
        return new ObjectNameId(e.getId(), e.getInternalName());
    }

    protected ServiceProviderService getServiceProviderService() {
        return serviceProviderService;
    }

    protected abstract C toConfig(E entity);

    protected abstract void fromConfig(C config, E entity);

    protected C newConfig() {
        return null;
    }

}
