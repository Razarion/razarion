package com.btxtech.server.collada;

import com.btxtech.client.terrain.VertexList;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverter {

    public static VertexList read(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException, ColladaException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputStream);

        Collada collada = new Collada(doc);

        return collada.generateVertexList();
    }

}
