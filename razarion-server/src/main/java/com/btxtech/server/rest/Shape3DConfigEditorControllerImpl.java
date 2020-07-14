package com.btxtech.server.rest;

import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.server.collada.Shape3DBuilder;
import com.btxtech.server.persistence.AbstractCrudPersistence;
import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.shape.Shape3DComposite;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.rest.Shape3DConfigEditorController;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by Beat
 * 16.08.2016.
 */
public class Shape3DConfigEditorControllerImpl extends AbstractCrudController<Shape3DConfig, ColladaEntity> implements Shape3DConfigEditorController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Shape3DCrudPersistence shape3DPersistence;

    // TODO @Override
    @SecurityCheck
    public Shape3DComposite colladaConvert(int id, String colladaString) {
        try {
            Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(colladaString, null);
            return new Shape3DComposite().setShape3D(shape3DBuilder.createShape3D(id)).setVertexContainerBuffers(shape3DBuilder.createVertexContainerBuffer(id));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    protected AbstractCrudPersistence getCrudPersistence() {
        return shape3DPersistence;
    }
}
