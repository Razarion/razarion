package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.Shape3DMaterialConfig;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.dto.PhongMaterialConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        shape3D.id(id).internalName(internalName);

        List<Element3D> element3Ds = createElement3DS(id);
        if (colladaConverterMapper != null) {
            fillElement3Ds(element3Ds);
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

    private void fillElement3Ds(List<Element3D> element3Ds) {
        for (Element3D element3D : element3Ds) {
            if (element3D.getVertexContainers() == null) {
                continue;
            }
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                String materialId = vertexContainer.getShape3DMaterialConfig().getMaterialId();
                PhongMaterialConfig phongMaterialConfig = vertexContainer.getShape3DMaterialConfig().getPhongMaterialConfig();
                if (materialId != null && phongMaterialConfig != null) {
                    phongMaterialConfig.setScale(1.0);
                    phongMaterialConfig.setTextureId(colladaConverterMapper.getTextureId(materialId));
                    phongMaterialConfig.setBumpMapId(colladaConverterMapper.getBumpMapId(materialId));
                    phongMaterialConfig.setBumpMapDepth(colladaConverterMapper.getBumpMapDepth(materialId));
                }
                vertexContainer.getShape3DMaterialConfig().setAlphaToCoverage(colladaConverterMapper.getAlphaToCoverage(materialId));
                vertexContainer.getShape3DMaterialConfig().setCharacterRepresenting(colladaConverterMapper.isCharacterRepresenting(materialId));
            }
        }
    }

    private List<Element3D> createElement3DS(int id) {
        return element3DBuilders.stream().map(element3DBuilder -> element3DBuilder.createElement3D(id)).collect(Collectors.toList());
    }

    public List<VertexContainerBuffer> createVertexContainerBuffer(int shape3DId) {
        List<VertexContainerBuffer> vertexContainerBuffers = new ArrayList<>();
        for (Element3DBuilder element3DBuilder : element3DBuilders) {
            vertexContainerBuffers.addAll(element3DBuilder.createVertexContainerBuffers(shape3DId));
        }
        return vertexContainerBuffers;
    }

    public Shape3DConfig createShape3DConfig(int id) {
        Shape3DConfig shape3DConfig = new Shape3DConfig().id(id).internalName(internalName);

        List<Element3D> element3Ds = createElement3DS(id);
        if (colladaConverterMapper != null) {
            fillElement3Ds(element3Ds);
        }

        List<Shape3DMaterialConfig> shape3DMaterialConfigs = new ArrayList<>();
        element3Ds.forEach(element3D -> element3D.getVertexContainers().forEach(vertexContainer -> {
            shape3DMaterialConfigs.add(new Shape3DMaterialConfig()
                    .materialId(vertexContainer.getShape3DMaterialConfig().getMaterialId())
                    .materialName(vertexContainer.getShape3DMaterialConfig().getMaterialName())
                    .phongMaterialConfig(vertexContainer.getShape3DMaterialConfig().getPhongMaterialConfig())
                    .characterRepresenting(vertexContainer.getShape3DMaterialConfig().isCharacterRepresenting())
                    .alphaToCoverage(vertexContainer.getShape3DMaterialConfig().getAlphaToCoverage()));
        }));
        shape3DConfig.setShape3DMaterialConfigs(shape3DMaterialConfigs);
        return shape3DConfig;
    }

}
