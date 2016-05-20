package com.btxtech.server.itemtype;

import com.btxtech.server.ExceptionHandler;
import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.shared.ItemTypeService;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.dto.ItemType;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 15.08.2015.
 */
@Service
@ApplicationScoped
public class ItemTypeServiceImpl implements ItemTypeService {
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Collection<ItemType> loadItemTypes() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<ItemTypeEntity> userQuery = criteriaBuilder.createQuery(ItemTypeEntity.class);
            Root<ItemTypeEntity> from = userQuery.from(ItemTypeEntity.class);
            CriteriaQuery<ItemTypeEntity> userSelect = userQuery.select(from);
            List<ItemTypeEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

            Collection<ItemType> itemTypes = new ArrayList<>();
            for (ItemTypeEntity itemTypeEntity : itemTypeEntities) {
                itemTypes.add(ColladaConverter.convertToItemType(itemTypeEntity));
            }
            return itemTypes;

        } catch (RuntimeException e) {
            exceptionHandler.handleException(e);
            throw e;
        } catch (Exception e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
