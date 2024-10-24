package com.btxtech.server.rest;

import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.server.collada.Shape3DBuilder;
import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.datatypes.shape.Shape3DComposite;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.rest.Shape3DController;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public class Shape3DControllerImpl implements Shape3DController {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Shape3DCrudPersistence shape3DPersistence;

    @Override
    public List<VertexContainerBuffer> getVertexBuffer() {
        try {
            return shape3DPersistence.getVertexContainerBuffers();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    @SecurityCheck
    public Shape3DComposite colladaConvert(Shape3DConfig shape3DConfig) {
        try {
            Shape3DBuilder shape3DBuilder = ColladaConverter.createShape3DBuilder(shape3DConfig.getColladaString(), null, shape3DConfig);
            return new Shape3DComposite()
                    .shape3DConfig(shape3DBuilder.createShape3DConfig(shape3DConfig.getId()))
                    .shape3D(shape3DBuilder.createShape3D(shape3DConfig.getId()))
                    .vertexContainerBuffers(shape3DBuilder.createVertexContainerBuffer(shape3DConfig.getId()));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

}
