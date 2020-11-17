package com.btxtech.server.collada;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.datatypes.shape.VertexContainerBuffer;
import com.btxtech.shared.datatypes.shape.config.Shape3DAnimationTriggerConfig;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;
import com.btxtech.shared.datatypes.shape.config.Shape3DElementConfig;
import com.btxtech.shared.datatypes.shape.config.Shape3DMaterialConfig;
import com.btxtech.shared.dto.PhongMaterialConfig;
import com.btxtech.shared.utils.Shape3DUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 08.03.2017.
 */
public class Shape3DBuilder {
    private int id;
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

        List<Element3D> element3Ds = createElement3DS(id);
        if (colladaConverterMapper != null) {
            fillElement3Ds(element3Ds);
        }

        MapList<Element3D, ModelMatrixAnimation> modelMatrixAnimations = new MapList<>();
        if (animations != null) {
            for (Animation animation : animations) {
                modelMatrixAnimations.put(Shape3DUtils.getElement3D(animation.getChannelTargetId(), element3Ds),
                        animation.convert());
            }
        }
        if (colladaConverterMapper != null) {
            modelMatrixAnimations.iterate((element3D, modelMatrixAnimation) -> {
                modelMatrixAnimation.setAnimationTrigger(colladaConverterMapper.getAnimationTrigger(modelMatrixAnimation.getId()));
                return true;
            });
        }
        element3Ds.forEach(element3D -> element3D.setModelMatrixAnimations(modelMatrixAnimations.get(element3D)));

        return new Shape3D().id(id).element3Ds(element3Ds);
    }

    public Shape3DConfig createShape3DConfig(int id) {
        Shape3D shape3D = createShape3D(id);
        Shape3DConfig shape3DConfig = new Shape3DConfig()
                .id(id)
                .internalName(internalName);
        if (shape3D.getElement3Ds() != null) {
            shape3DConfig.setShape3DElementConfigs(shape3D.getElement3Ds()
                    .stream()
                    .map(element3D -> new Shape3DElementConfig()
                            .shape3DMaterialConfigs(element3D.getVertexContainers().stream()
                                    .map(vertexContainer -> new Shape3DMaterialConfig()
                                            .materialId(vertexContainer.getShape3DMaterial().getMaterialId())
                                            .materialName(vertexContainer.getShape3DMaterial().getMaterialName())
                                            .phongMaterialConfig(vertexContainer.getShape3DMaterial().getPhongMaterialConfig())
                                            .characterRepresenting(vertexContainer.getShape3DMaterial().isCharacterRepresenting())
                                            .alphaToCoverage(vertexContainer.getShape3DMaterial().getAlphaToCoverage()))
                                    .collect(Collectors.toList()))
                            .shape3DAnimationTriggerConfigs(setupAnimationTriggerConfigs(element3D)))
                    .collect(Collectors.toList()));
        }
        return shape3DConfig;
    }

    private List<Shape3DAnimationTriggerConfig> setupAnimationTriggerConfigs(Element3D element3D) {
        if (element3D.getModelMatrixAnimations() == null) {
            return null;
        }
        return element3D.getModelMatrixAnimations().stream()
                .map(modelMatrixAnimation -> new Shape3DAnimationTriggerConfig()
                        .description(modelMatrixAnimation.getId())
                        .animationTrigger(modelMatrixAnimation.getAnimationTrigger()))
                .collect(Collectors.toList());
    }

    public List<VertexContainerBuffer> createVertexContainerBuffer(int shape3DId) {
        List<VertexContainerBuffer> vertexContainerBuffers = new ArrayList<>();
        for (Element3DBuilder element3DBuilder : element3DBuilders) {
            vertexContainerBuffers.addAll(element3DBuilder.createVertexContainerBuffers(shape3DId));
        }
        return vertexContainerBuffers;
    }

    private void fillElement3Ds(List<Element3D> element3Ds) {
        for (Element3D element3D : element3Ds) {
            if (element3D.getVertexContainers() == null) {
                continue;
            }
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                String materialId = vertexContainer.getShape3DMaterial().getMaterialId();
                PhongMaterialConfig phongMaterialConfig = vertexContainer.getShape3DMaterial().getPhongMaterialConfig();
                if (materialId != null && phongMaterialConfig != null) {
                    phongMaterialConfig.setScale(1.0);
                    phongMaterialConfig.setTextureId(colladaConverterMapper.getTextureId(materialId));
                    phongMaterialConfig.setBumpMapId(colladaConverterMapper.getBumpMapId(materialId));
                    phongMaterialConfig.setBumpMapDepth(colladaConverterMapper.getBumpMapDepth(materialId));
                }
                vertexContainer.getShape3DMaterial().setAlphaToCoverage(colladaConverterMapper.getAlphaToCoverage(materialId));
                vertexContainer.getShape3DMaterial().setCharacterRepresenting(colladaConverterMapper.isCharacterRepresenting(materialId));
            }
        }
    }

    private List<Element3D> createElement3DS(int id) {
        return element3DBuilders.stream().map(element3DBuilder -> element3DBuilder.createElement3D(id)).collect(Collectors.toList());
    }
}
