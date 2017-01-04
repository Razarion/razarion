package com.btxtech.servercommon;

import com.btxtech.servercommon.collada.ColladaException;
import com.btxtech.shared.dto.GameUiControlConfig;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by Beat
 * 06.07.2016.
 */
public interface GameUiControlConfigPersistence {

    GameUiControlConfig load() throws ParserConfigurationException, ColladaException, SAXException, IOException;

}
