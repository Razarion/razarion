package com.btxtech.server.collada;

import com.btxtech.server.itemtype.ItemTypeEntity;
import com.btxtech.server.terrain.object.TerrainObjectEntity;
import com.btxtech.shared.dto.ItemType;
import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverter {
    private static Logger LOGGER = Logger.getLogger(ColladaConverter.class.getName());

    public static TerrainObject convertToTerrainObject(final TerrainObjectEntity terrainObjectEntity, String colladaString) throws ParserConfigurationException, SAXException, ColladaException, IOException {
        Collada collada = createCollada(colladaString);

        final Collection<VertexContainer> vertexContainers = new ArrayList<>();

        collada.convert(new ColladaConverterControl() {
            @Override
            protected void onNewVertexContainer(VertexContainer vertexContainer) {
                vertexContainer.setTextureId(terrainObjectEntity.getTextureId(vertexContainer.getMaterialId()));
                vertexContainers.add(vertexContainer);
            }
        });

        TerrainObject terrainObject = new TerrainObject();
        terrainObject.setId(terrainObjectEntity.getId().intValue());
        terrainObject.setVertexContainers(vertexContainers);
        return terrainObject;
    }

    public static TerrainObject convertToTerrainObject(final TerrainObjectEntity terrainObjectEntity) throws ParserConfigurationException, SAXException, ColladaException, IOException {
        return convertToTerrainObject(terrainObjectEntity, terrainObjectEntity.getColladaString());
    }

    public static ItemType convertToItemType(ItemTypeEntity itemTypeEntity) throws ParserConfigurationException, SAXException, ColladaException, IOException {
        Collada collada = createCollada(itemTypeEntity.getColladaString());

        final List<Vertex> vertices = new ArrayList<>();
        final List<Vertex> norms = new ArrayList<>();
        final List<TextureCoordinate> textureCoordinates = new ArrayList<>();

        collada.convert(new ColladaConverterControl() {
            @Override
            protected void onNewVertexContainer(VertexContainer vertexContainer) {
                vertices.addAll(vertexContainer.getVertices());
                norms.addAll(vertexContainer.getNorms());
                textureCoordinates.addAll(vertexContainer.getTextureCoordinates());
            }
        });
        return new ItemType(itemTypeEntity.getId().intValue(), new VertexContainer(null, null, vertices, norms, textureCoordinates, null, null, null, null));
    }

    private static Collada createCollada(String colladaString) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(colladaString)));

        LOGGER.finest("Start Parsing");
        return new Collada(doc);
    }
}
