package com.btxtech.server.collada;

import com.btxtech.server.terrain.object.TerrainObjectEntity;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.VertexContainer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverter {
    private static Logger LOGGER = Logger.getLogger(ColladaConverter.class.getName());

    public static TerrainObject convertToTerrainObject(final TerrainObjectEntity terrainObjectEntity) throws ParserConfigurationException, SAXException, ColladaException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(terrainObjectEntity.getColladaString())));

        LOGGER.finest("Start Parsing");
        Collada collada = new Collada(doc);

        final Map<TerrainObject.Type, VertexContainer> containers = new HashMap<>();
        collada.convert(new ColladaConverterControl() {
            @Override
            protected void onNewVertexContainer(String name, VertexContainer vertexContainer) {
                containers.put(terrainObjectEntity.nameToType(name), vertexContainer);
            }
        });

        return new TerrainObject(terrainObjectEntity.getId().intValue(), containers);
    }

}
