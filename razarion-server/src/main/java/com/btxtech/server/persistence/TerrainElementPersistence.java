package com.btxtech.server.persistence;

import com.btxtech.server.persistence.object.TerrainObjectEntity;
import com.btxtech.server.persistence.object.TerrainObjectEntity_;
import com.btxtech.server.persistence.surface.GroundConfigEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity;
import com.btxtech.server.persistence.surface.SlopeConfigEntity_;
import com.btxtech.servercommon.collada.ColladaConverter;
import com.btxtech.servercommon.collada.ColladaException;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import org.xml.sax.SAXException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class TerrainElementPersistence {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public GroundSkeletonConfig loadGroundSkeleton() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
        Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
        CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getSingleResult().generateGroundSkeleton();
    }

    @Transactional
    public GroundConfig loadGroundConfig() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
        Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
        CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
        return entityManager.createQuery(userSelect).getSingleResult().toGroundConfig();
    }

    @Transactional
    public GroundConfig saveGroundConfig(GroundConfig groundConfig) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<GroundConfigEntity> userQuery = criteriaBuilder.createQuery(GroundConfigEntity.class);
        Root<GroundConfigEntity> from = userQuery.from(GroundConfigEntity.class);
        CriteriaQuery<GroundConfigEntity> userSelect = userQuery.select(from);
        GroundConfigEntity groundConfigEntity = entityManager.createQuery(userSelect).getSingleResult();
        groundConfigEntity.fromGroundConfig(groundConfig);
        return entityManager.merge(groundConfigEntity).toGroundConfig();
    }

    @Transactional
    public List<ObjectNameId> getSlopeNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<SlopeConfigEntity> root = cq.from(SlopeConfigEntity.class);
        cq.multiselect(root.get(SlopeConfigEntity_.id), root.get(SlopeConfigEntity_.internalName));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        List<ObjectNameId> objectNameIds = new ArrayList<>();
        for (Tuple t : tupleResult) {
            objectNameIds.add(new ObjectNameId(((Long) t.get(0)).intValue(), (String) t.get(1)));
        }
        return objectNameIds;
    }

    @Transactional
    public SlopeConfig loadSlopeConfig(int id) {
        return entityManager.find(SlopeConfigEntity.class, (long) id).toSlopeConfig();
    }

    @Transactional
    public List<SlopeSkeletonConfig> loadSlopeSkeletons() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SlopeConfigEntity> userQuery = criteriaBuilder.createQuery(SlopeConfigEntity.class);
        Root<SlopeConfigEntity> root = userQuery.from(SlopeConfigEntity.class);
        CriteriaQuery<SlopeConfigEntity> userSelect = userQuery.select(root);
        Collection<SlopeConfigEntity> slopeConfigEntities = entityManager.createQuery(userSelect).getResultList();

        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        for (SlopeConfigEntity slopeConfigEntity : slopeConfigEntities) {
            slopeSkeletonConfigs.add(slopeConfigEntity.toSlopeSkeleton());
        }

        return slopeSkeletonConfigs;
    }

    @Transactional
    public SlopeConfig saveSlopeConfig(SlopeConfig slopeConfig) {
        SlopeConfigEntity slopeConfigEntity;
        if (slopeConfig.hasId()) {
            slopeConfigEntity = entityManager.find(SlopeConfigEntity.class, (long) slopeConfig.getId());
        } else {
            slopeConfigEntity = new SlopeConfigEntity();
        }
        slopeConfigEntity.fromSlopeConfig(slopeConfig);

        return entityManager.merge(slopeConfigEntity).toSlopeConfig();
    }

    @Transactional
    public void deleteSlopeConfig(SlopeConfig slopeConfig) {
        SlopeConfigEntity slopeConfigEntity = entityManager.find(SlopeConfigEntity.class, (long) slopeConfig.getId());
        entityManager.remove(slopeConfigEntity);
    }

    @Transactional
    public List<ObjectNameId> getTerrainObjectNameIds() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = criteriaBuilder.createTupleQuery();
        Root<TerrainObjectEntity> root = cq.from(TerrainObjectEntity.class);
        cq.multiselect(root.get(TerrainObjectEntity_.id), root.get(TerrainObjectEntity_.internalName));
        List<Tuple> tupleResult = entityManager.createQuery(cq).getResultList();
        List<ObjectNameId> objectNameIds = new ArrayList<>();
        for (Tuple t : tupleResult) {
            objectNameIds.add(new ObjectNameId(((Long) t.get(0)).intValue(), (String) t.get(1)));
        }
        return objectNameIds;
    }

    @Transactional
    public List<TerrainObjectConfig> loadTerrainObjects() throws ParserConfigurationException, ColladaException, SAXException, IOException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<TerrainObjectEntity> userQuery = criteriaBuilder.createQuery(TerrainObjectEntity.class);
        Root<TerrainObjectEntity> from = userQuery.from(TerrainObjectEntity.class);
        CriteriaQuery<TerrainObjectEntity> userSelect = userQuery.select(from);
        List<TerrainObjectEntity> terrainObjectEntities = entityManager.createQuery(userSelect).getResultList();

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        for (TerrainObjectEntity terrainObjectEntity : terrainObjectEntities) {
            TerrainObjectConfig terrainObjectConfig = terrainObjectEntity.terrainObjectConfig();
            terrainObjectConfig.setShape3D(ColladaConverter.convertShape3D(terrainObjectEntity.getColladaString(), terrainObjectEntity));
            terrainObjectConfigs.add(terrainObjectConfig);
        }
        return terrainObjectConfigs;
    }

    @Transactional
    public void saveTerrainObject(int id, String colladaString, Map<String, Integer> textures) {
        TerrainObjectEntity terrainObjectEntity = entityManager.find(TerrainObjectEntity.class, (long) id);
        if (colladaString != null) {
            terrainObjectEntity.setColladaString(colladaString);
        }
        Map<String, ImageLibraryEntity> textureEntities = new HashMap<>();
        for (Map.Entry<String, Integer> entry : textures.entrySet()) {
            textureEntities.put(entry.getKey(), entityManager.find(ImageLibraryEntity.class, entry.getValue().longValue()));
        }
        terrainObjectEntity.setTextures(textureEntities);
        entityManager.persist(terrainObjectEntity);
    }

    @Transactional
    public TerrainObjectConfig colladaConvert(int terrainObjectId, String colladaString) throws ParserConfigurationException, ColladaException, SAXException, IOException {
//     TODO   TerrainObjectEntity terrainObjectEntity = entityManager.find(TerrainObjectEntity.class, (long) terrainObjectId);
//   TODO     ColladaConverterInput input = new ColladaConverterInput();
//    TODO    input.setColladaString(colladaString).setId(terrainObjectEntity.getId().intValue()).setTextureMapper(terrainObjectEntity);
//   TODO     return ColladaConverter.convertToTerrainObject(input);
        throw new UnsupportedOperationException();
    }

}
