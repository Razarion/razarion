package com.btxtech.server.rest;

import com.btxtech.servercommon.collada.ColladaConverter;
import com.btxtech.shared.EditorHelper;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by Beat
 * 16.08.2016.
 */
public class EditorHelperImpl implements EditorHelper {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

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
}
