package com.btxtech.server.terrain;

import com.btxtech.client.TerrainEditorService;
import org.jboss.errai.bus.server.annotations.Service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.11.2015.
 */
@Service
@ApplicationScoped
public class TerrainEditorServiceImpl implements TerrainEditorService {
    @Inject
    private Logger logger;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void save() {
        entityManager.persist(new PlateauEntity());
    }
}
