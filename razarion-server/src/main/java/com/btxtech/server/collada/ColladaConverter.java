package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

    public static Shape3DBuilder createShape3DBuilder(String colladaText, ColladaConverterMapper colladaConverterMapper, Shape3DConfig source) throws IOException, SAXException, ParserConfigurationException {
        if (colladaText == null || colladaText.isEmpty()) {
            return new Shape3DBuilder();
        }

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(colladaText)));

        LOGGER.finest("Start Parsing");
        return new Collada(doc).create()
                .colladaConverterMapper(colladaConverterMapper)
                .source(source);
    }
}
