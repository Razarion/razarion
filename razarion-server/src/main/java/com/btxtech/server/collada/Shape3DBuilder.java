package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 08.03.2017.
 */
public class Shape3DBuilder {
    private String internalName;
    private List<Element3DBuilder> element3DBuilders = new ArrayList<>();
    private ColladaConverterMapper colladaConverterMapper;
    private Collection<Animation> animations;

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setColladaConverterMapper(ColladaConverterMapper colladaConverterMapper) {
        this.colladaConverterMapper = colladaConverterMapper;
    }

    public void addElement3DBuilder(Element3DBuilder element3DBuilder) {
        element3DBuilders.add(element3DBuilder);
    }

    public void setAnimations(Collection<Animation> animations) {
        this.animations = animations;
    }

    public Shape3D createShape3D(int id) {
        Shape3D shape3D = new Shape3D();
        shape3D.setDbId(id).setInternalName(internalName);

        List<Element3D> element3Ds = new ArrayList<>();
        for (Element3DBuilder element3DBuilder : element3DBuilders) {
            element3Ds.add(element3DBuilder.createElement3D(id));
        }

        if (colladaConverterMapper != null) {
            for (Element3D element3D : element3Ds) {
                if (element3D.getVertexContainers() == null) {
                    continue;
                }
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    String materialId = vertexContainer.getMaterialId();
                    if (materialId != null) {
                        vertexContainer.setTextureId(colladaConverterMapper.getTextureId(materialId));
                        vertexContainer.setAlphaCutout(colladaConverterMapper.getAlphaCutout(materialId));
                    }
                    vertexContainer.setCharacterRepresenting(colladaConverterMapper.isCharacterRepresenting(materialId));
                }
            }
        }
        shape3D.setElement3Ds(element3Ds);
        List<ModelMatrixAnimation> modelMatrixAnimations = new ArrayList<>();
        if (animations != null) {
            for (Animation animation : animations) {
                ModelMatrixAnimation modelMatrixAnimation = animation.convert(shape3D);
                modelMatrixAnimations.add(modelMatrixAnimation);
            }
        }
        if (colladaConverterMapper != null) {
            for (ModelMatrixAnimation modelMatrixAnimation : modelMatrixAnimations) {
                modelMatrixAnimation.setAnimationTrigger(colladaConverterMapper.getAnimationTrigger(modelMatrixAnimation.getId()));
            }
        }
        if (!modelMatrixAnimations.isEmpty()) {
            shape3D.setModelMatrixAnimations(modelMatrixAnimations);
        }

        return shape3D;
    }

    public List<VertexContainerBuffer> createVertexContainerBuffer(int shape3DId) {
        List<VertexContainerBuffer> vertexContainerBuffers = new ArrayList<>();
        for (Element3DBuilder element3DBuilder : element3DBuilders) {
            vertexContainerBuffers.addAll(element3DBuilder.createVertexContainerBuffers(shape3DId));
        }
        return vertexContainerBuffers;
    }


}
