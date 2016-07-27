package com.btxtech.servercommon.collada;

import com.btxtech.shared.dto.TerrainObject;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
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

    public static TerrainObject convertToTerrainObject(final ColladaConverterInput input) throws ParserConfigurationException, SAXException, ColladaException, IOException {
        Collada collada = createCollada(input.getColladaString());

        final Collection<VertexContainer> vertexContainers = new ArrayList<>();

        collada.convert(new ColladaConverterControl() {
            @Override
            protected void onNewVertexContainer(VertexContainer vertexContainer) {
                vertexContainer.setTextureId(input.getTextureId(vertexContainer.getMaterialId()));
                vertexContainers.add(vertexContainer);
            }
        });

        TerrainObject terrainObject = new TerrainObject();
        terrainObject.setId(input.getId());
        terrainObject.setVertexContainers(vertexContainers);
        return terrainObject;
    }

    public static ItemType convertToItemType(ColladaConverterInput input) throws ParserConfigurationException, SAXException, ColladaException, IOException {
        Collada collada = createCollada(input.getColladaString());

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

        return new BaseItemType().setVertexContainer(new VertexContainer().setVertices(vertices).setNorms(norms).setTextureCoordinates(textureCoordinates)).setId(input.getId());
    }

    private static Collada createCollada(String colladaString) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(colladaString)));

        LOGGER.finest("Start Parsing");
        return new Collada(doc);
    }
}
