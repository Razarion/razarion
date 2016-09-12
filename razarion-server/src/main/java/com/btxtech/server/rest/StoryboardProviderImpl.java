package com.btxtech.server.rest;

import com.btxtech.servercommon.StoryboardPersistence;
import com.btxtech.servercommon.collada.ColladaException;
import com.btxtech.shared.StoryboardProvider;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by Beat
 * 06.07.2016.
 */
public class StoryboardProviderImpl implements StoryboardProvider {
    @Inject
    // @Emulation
    private StoryboardPersistence storyboardPersistence;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    @Transactional
    public StoryboardConfig loadStoryboard() {
        try {
            return storyboardPersistence.load();
        } catch (ParserConfigurationException | ColladaException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}
