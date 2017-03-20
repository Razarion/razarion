package com.btxtech.server.marketing;

import com.btxtech.server.user.SecurityCheck;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * Created by Beat
 * 19.03.2017.
 */

// If move to the web-server: remove all weld and Hibernate dependency and persistence.xml

@ApplicationScoped
public class MarketingService {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @SecurityCheck
    public void startAd() {
        CurrentAdSetEntity currentAdSetEntity = new CurrentAdSetEntity();
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("primary");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        entityManager.persist(currentAdSetEntity);
        entityManager.getTransaction().commit();
        entityManager.close();
        entityManagerFactory.close();
    }

    @Transactional
    @SecurityCheck
    public void stopAdAndHistorize() {

    }

}
