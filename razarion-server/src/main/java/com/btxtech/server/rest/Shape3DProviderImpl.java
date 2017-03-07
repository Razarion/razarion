package com.btxtech.server.rest;

import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.servercommon.collada.ColladaConverter;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.dto.FastVertexContainer;
import com.btxtech.shared.rest.Shape3DProvider;
import com.btxtech.shared.system.ExceptionHandler;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 16.08.2016.
 */
public class Shape3DProviderImpl implements Shape3DProvider {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Shape3DPersistence shape3DPersistence;

    @Override
    public List<FastVertexContainer> getVertexBuffer() {
        try {
            List<FastVertexContainer> fastVertexContainers = new ArrayList<>();
            for (Shape3D shape3D : shape3DPersistence.getShape3Ds()) {
                if (shape3D.getElement3Ds() != null) {
                    for (Element3D element3D : shape3D.getElement3Ds()) {
                        if (element3D.getVertexContainers() != null) {
                            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                                FastVertexContainer fastVertexContainer = new FastVertexContainer();
                                fastVertexContainer.setId(vertexContainer.getKey());
                                fastVertexContainer.setVertexData(vertices2Floats(vertexContainer.getVertices()));
                                fastVertexContainer.setNormData(vertices2Floats(vertexContainer.getNorms()));
                                fastVertexContainer.setTextureCoordinate(textureCoordinates2Floats(vertexContainer.getTextureCoordinates()));
                                fastVertexContainers.add(fastVertexContainer);
                            }
                        }
                    }
                }
            }
            return fastVertexContainers;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    private List<Float> vertices2Floats(List<Vertex> vertices) {
        List<Float> floatListList = new ArrayList<>();
        for (Vertex vertex : vertices) {
            floatListList.add((float) vertex.getX());
            floatListList.add((float) vertex.getY());
            floatListList.add((float) vertex.getZ());
        }
        return floatListList;
    }


    private List<Float> textureCoordinates2Floats(List<TextureCoordinate> textureCoordinates) {
        List<Float> floatListList = new ArrayList<>();
        for (TextureCoordinate textureCoordinate : textureCoordinates) {
            floatListList.add((float) textureCoordinate.getS());
            floatListList.add((float) textureCoordinate.getT());
        }
        return floatListList;
    }

    @Override
    public List<Shape3D> getShape3Ds() {
        try {
            return shape3DPersistence.getShape3Ds();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public Shape3D create() {
        try {
            return shape3DPersistence.create();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public Shape3D colladaConvert(String colladaString) {
        try {
            return ColladaConverter.convertShape3D(0, colladaString, null);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void save(Shape3DConfig shape3DConfig) {
        try {
            shape3DPersistence.save(shape3DConfig);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            exceptionHandler.handleException(e);
            throw new RuntimeException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public void delete(int id) {
        try {
            shape3DPersistence.delete(id);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
