package com.btxtech.servercommon.collada;

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

    public static Shape3DBuilder createShape3DBuilder(String colladaText, ColladaConverterMapper colladaConverterMapper) throws IOException, SAXException, ParserConfigurationException {
        if (colladaText == null || colladaText.isEmpty()) {
            return new Shape3DBuilder();
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(colladaText)));

        LOGGER.finest("Start Parsing");
        Shape3DBuilder shape3DBuilder = new Collada(doc).create();
        shape3DBuilder.setColladaConverterMapper(colladaConverterMapper);
        return shape3DBuilder;
    }
}
