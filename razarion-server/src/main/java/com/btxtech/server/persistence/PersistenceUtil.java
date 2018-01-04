package com.btxtech.server.persistence;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * 21.10.2016.
 */
public interface PersistenceUtil {
    static Integer getImageIdSafe(ImageLibraryEntity imageLibraryEntity) {
        if (imageLibraryEntity != null) {
            return imageLibraryEntity.getId();
        } else {
            return null;
        }
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
}
