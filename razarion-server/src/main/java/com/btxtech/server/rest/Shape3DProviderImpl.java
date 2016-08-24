package com.btxtech.server.rest;

import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.servercommon.collada.ColladaConverter;
import com.btxtech.shared.Shape3DProvider;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * Created by Beat
 * 16.08.2016.
 */
public class Shape3DProviderImpl implements Shape3DProvider {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Shape3DPersistence shape3DPersistence;

    @Override
    public List<Shape3D> getShape3Ds() {
        try {
            return shape3DPersistence.getShape3Ds();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public Shape3D create() {
        try {
            return shape3DPersistence.create();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public Shape3D colladaConvert(String colladaString) {
        try {
            return ColladaConverter.convertShape3D(colladaString, null);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void save(Shape3DConfig shape3DConfig) {
        try {
            shape3DPersistence.save(shape3DConfig);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void delete(int id) {
        try {
            shape3DPersistence.delete(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
