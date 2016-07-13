package com.btxtech.server.rest;

import com.btxtech.servercommon.collada.ColladaException;
import com.btxtech.server.persistence.TerrainElementPersistenceService;
import com.btxtech.shared.TerrainElementService;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.SlopeConfig;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 20.11.2015.
 */
public class TerrainElementServiceImpl implements TerrainElementService {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TerrainElementPersistenceService persistenceService;

    @Override
    public List<ObjectNameId> getSlopeNameIds() {
        try {
            return persistenceService.getSlopeNameIds();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public SlopeConfig loadSlopeConfig(int id) {
        try {
            return persistenceService.loadSlopeConfig(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public SlopeConfig saveSlopeConfig(SlopeConfig slopeConfig) {
        try {
            return persistenceService.saveSlopeConfig(slopeConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void deleteSlopeConfig(SlopeConfig slopeConfig) {
        try {
            persistenceService.deleteSlopeConfig(slopeConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public GroundConfig loadGroundConfig() {
        try {
            return persistenceService.loadGroundConfig();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public GroundConfig saveGroundConfig(GroundConfig slopeConfig) {
        try {
            return persistenceService.saveGroundConfig(slopeConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<ObjectNameId> getTerrainObjectNameIds() {
        try {
            return persistenceService.getTerrainObjectNameIds();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void saveTerrainObject(int id, String colladaString, Map<String, Integer> textures) {
        try {
            persistenceService.saveTerrainObject(id, colladaString, textures);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public TerrainObject colladaConvert(int terrainObjectId, String colladaString) {
        try {
            return persistenceService.colladaConvert(terrainObjectId, colladaString);
        } catch (ParserConfigurationException | ColladaException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
