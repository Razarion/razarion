package com.btxtech.server.collada;

import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverter {
    private static Logger LOGGER = Logger.getLogger(ColladaConverter.class.getName());

    public static List<VertexList> read(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException, ColladaException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputStream);

        LOGGER.finest("Start Parsing");
        Collada collada = new Collada(doc);

        List<VertexList> vertexLists = collada.generateVertexList();
        // for (VertexList vertexList : vertexLists) {
        //    vertexList.multiply(Matrix4.createScale(20, 20, 20));
        // }
        return vertexLists;
    }

}
