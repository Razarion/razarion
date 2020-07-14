package com.btxtech.server.rest;

import com.btxtech.server.persistence.Shape3DCrudPersistence;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
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

}
