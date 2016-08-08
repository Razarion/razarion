package com.btxtech.servercommon;

import com.btxtech.servercommon.collada.ColladaException;
import com.btxtech.shared.dto.StoryboardConfig;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by Beat
 * 06.07.2016.
 */
public interface StoryboardPersistence {

    StoryboardConfig load() throws ParserConfigurationException, ColladaException, SAXException, IOException;

}
