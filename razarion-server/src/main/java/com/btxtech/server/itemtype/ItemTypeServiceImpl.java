package com.btxtech.server.itemtype;

import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.system.ExceptionHandler;

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
@Deprecated // Should be a rest service
public class ItemTypeServiceImpl {
    @Inject
    private ExceptionHandler exceptionHandler;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Collection<ItemType> loadItemTypes() {
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<ItemTypeEntity> userQuery = criteriaBuilder.createQuery(ItemTypeEntity.class);
            Root<ItemTypeEntity> from = userQuery.from(ItemTypeEntity.class);
            CriteriaQuery<ItemTypeEntity> userSelect = userQuery.select(from);
            List<ItemTypeEntity> itemTypeEntities = entityManager.createQuery(userSelect).getResultList();

            Collection<ItemType> itemTypes = new ArrayList<>();
     //       Gson gson = new Gson();
            for (ItemTypeEntity itemTypeEntity : itemTypeEntities) {
//   TODO             ColladaConverter.convertShape3D(itemTypeEntity.getColladaString(), null);
//   TODO
//  TODO
//  TODO              ColladaConverterInput input = new ColladaConverterInput();
//  TODO              input.setColladaString(itemTypeEntity.getColladaString()).setId(itemTypeEntity.getId().intValue());
//  TODO              itemTypes.add(ColladaConverter.convertToItemType(input));
            }
            System.out.println("loadSItemTypes --------------------------------------------------------");
  //          String json = gson.toJson(itemTypes);
  //          System.out.println(json);
            System.out.println("--------------------------------------------------------");
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
