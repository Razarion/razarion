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
import com.btxtech.shared.datatypes.shape.config.VertexContainerMaterialConfig;
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
    private String internalName;
    private List<Element3DBuilder> element3DBuilders = new ArrayList<>();
    private ColladaConverterMapper colladaConverterMapper;
    private Shape3DConfig source;
    private Collection<Animation> animations;

    public Shape3DBuilder internalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public Shape3DBuilder colladaConverterMapper(ColladaConverterMapper colladaConverterMapper) {
        this.colladaConverterMapper = colladaConverterMapper;
        return this;
    }

    public Shape3DBuilder animations(Collection<Animation> animations) {
        this.animations = animations;
        return this;
    }

    public Shape3DBuilder source(Shape3DConfig source) {
        this.source = source;
        return this;
    }

    public void addElement3DBuilder(Element3DBuilder element3DBuilder) {
        element3DBuilders.add(element3DBuilder);
    }

    public Shape3D createShape3D(int id) {
        List<Element3D> element3Ds = createElement3DS(id);
        if (colladaConverterMapper != null) {
            fillMaterialsFromMapper(element3Ds);
        } else if (source != null) {
            Shape3DUtils.fillMaterialFromSource(element3Ds, source);
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
        // TODO copy animation from source
        element3Ds.forEach(element3D -> element3D.setModelMatrixAnimations(modelMatrixAnimations.get(element3D)));

        return new Shape3D().id(id).element3Ds(element3Ds);
    }

    public Shape3DConfig createShape3DConfig(int id) {
        Shape3D shape3D = createShape3D(id);
        Shape3DConfig shape3DConfig = new Shape3DConfig()
                .id(id)
                .internalName(source != null ? source.getInternalName() : internalName);
        if (shape3D.getElement3Ds() != null) {
            shape3DConfig.setShape3DElementConfigs(shape3D.getElement3Ds()
                    .stream()
                    .map(element3D -> new Shape3DElementConfig()
                            .id(element3D.getId())
                            .shape3DMaterialConfigs(element3D.getVertexContainers().stream()
                                    .filter(vertexContainer -> vertexContainer.getVertexContainerMaterial() != null)
                                    .map(vertexContainer ->
                                            new VertexContainerMaterialConfig()
                                                    .materialId(vertexContainer.getVertexContainerMaterial().getMaterialId())
                                                    .materialName(vertexContainer.getVertexContainerMaterial().getMaterialName())
                                                    .phongMaterialConfig(vertexContainer.getVertexContainerMaterial().getPhongMaterialConfig())
                                                    .characterRepresenting(vertexContainer.getVertexContainerMaterial().isCharacterRepresenting())
                                                    .alphaToCoverage(vertexContainer.getVertexContainerMaterial().getAlphaToCoverage()))
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

    private void fillMaterialsFromMapper(List<Element3D> element3Ds) {
        for (Element3D element3D : element3Ds) {
            if (element3D.getVertexContainers() == null) {
                continue;
            }
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                if (vertexContainer.getVertexContainerMaterial() != null) {
                    String materialId = vertexContainer.getVertexContainerMaterial().getMaterialId();
                    vertexContainer.getVertexContainerMaterial().override(colladaConverterMapper.toVertexContainerMaterial(materialId));
                }
            }
        }
    }

    private List<Element3D> createElement3DS(int id) {
        return element3DBuilders.stream().map(element3DBuilder -> element3DBuilder.createElement3D(id)).collect(Collectors.toList());
    }
}
