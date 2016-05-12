package com.btxtech.server.collada;

import com.btxtech.shared.dto.TerrainObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverter {
    private static Logger LOGGER = Logger.getLogger(ColladaConverter.class.getName());

    public static TerrainObject convertToTerrainObject(ColladaConverterControl colladaConverterControl) throws ParserConfigurationException, SAXException, ColladaException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(colladaConverterControl.getColladaString())));

        LOGGER.finest("Start Parsing");
        Collada collada = new Collada(doc);

        return collada.generateTerrainObject(colladaConverterControl);
    }

}
