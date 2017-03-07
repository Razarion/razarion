package com.btxtech.servercommon.collada;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
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

    public static Shape3D convertShape3D(int id, String colladaText, ColladaConverterMapper colladaConverterMapper) throws IOException, SAXException, ParserConfigurationException {
        if (colladaText == null || colladaText.isEmpty()) {
            return new Shape3D();
        }
        Shape3D shape3D = createCollada(colladaText).create(id);
        if (colladaConverterMapper != null) {
            for (Element3D element3D : shape3D.getElement3Ds()) {
                if (element3D.getVertexContainers() == null) {
                    continue;
                }
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    String materialId = vertexContainer.getMaterialId();
                    if (materialId != null) {
                        vertexContainer.setTextureId(colladaConverterMapper.getTextureId(materialId));
                    }
                }
            }
            if (shape3D.getModelMatrixAnimations() != null) {
                for (ModelMatrixAnimation modelMatrixAnimation : shape3D.getModelMatrixAnimations()) {
                    modelMatrixAnimation.setAnimationTrigger(colladaConverterMapper.getAnimationTrigger(modelMatrixAnimation.getId()));
                }
            }
        }
        return shape3D;
    }

    private static Collada createCollada(String colladaString) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(colladaString)));

        LOGGER.finest("Start Parsing");
        return new Collada(doc);
    }
}
