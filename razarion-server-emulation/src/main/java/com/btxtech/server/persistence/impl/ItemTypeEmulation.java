package com.btxtech.server.persistence.impl;

import com.btxtech.servercommon.collada.ColladaConverter;
import com.btxtech.servercommon.collada.ColladaConverterMapper;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemState;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.MovableType;
import com.btxtech.shared.system.ExceptionHandler;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 18.07.2016.
 */
@Singleton
public class ItemTypeEmulation {
    public enum Id {
        SIMPLE_MOVABLE
    }

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;

    public List<ItemType> createItemTypes() {
        List<ItemType> itemTypes = new ArrayList<>();
        itemTypes.add(createSimpleMovable());
        return itemTypes;
    }

    public BaseItemType createSimpleMovable() {
        BaseItemType builder = (BaseItemType) new BaseItemType().setName("Builder Emulation").setId(Id.SIMPLE_MOVABLE.ordinal()).setTerrainType(TerrainType.LAND);
        ColladaMapper mapper = new ColladaMapper();
        mapper.putTexture("Chassis_Material-material", 174014);
        mapper.putTexture("Wheel1Material-material", 174014);
        mapper.putTexture("Wheel2Material-material", 174014);
        mapper.putTexture("Wheel3Material-material", 174014);
        mapper.putTexture("Wheel4Material-material", 174014);
        builder.setShape3D(loadAndConvertShape3d("C:\\dev\\projects\\razarion\\code\\tmp\\ItemType1Mirror.dae", mapper));
        mapper = new ColladaMapper();
        mapper.putTexture("Beam_Material-material", 174014);
        mapper.putTexture("SphereMaterial-material", 174014);
        mapper.putAnimation("Sphere_scale_X", ItemState.BEAM_UP).putAnimation("Sphere_scale_Y", ItemState.BEAM_UP).putAnimation("Sphere_scale_Z", ItemState.BEAM_UP);
        mapper.putAnimation("Plane_location_X", ItemState.BEAM_UP).putAnimation("Plane_location_Y", ItemState.BEAM_UP).putAnimation("Plane_location_Z", ItemState.BEAM_UP);
        builder.setSpawnShape3D(loadAndConvertShape3d("C:\\dev\\projects\\razarion\\code\\tmp\\ArrivelBall01.dae", mapper)).setSpawnDurationMillis(5000);
        return builder.setMovableType(new MovableType().setSpeed(10)).setHealth(100).setRadius(50);
    }

    private Shape3D loadAndConvertShape3d(String fileName, ColladaConverterMapper colladaConverterMapper) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(fileName);
            String colladaString = IOUtils.toString(fileInputStream);
            return ColladaConverter.convertShape3D(colladaString, colladaConverterMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if(fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    exceptionHandler.handleException(e);
                }
            }
        }
    }

    private class ColladaMapper implements ColladaConverterMapper {
        private Map<String, Integer> textures = new HashMap<>();
        private Map<String, ItemState> animations = new HashMap<>();

        public ColladaMapper putTexture(String animationId, Integer imageId) {
            textures.put(animationId, imageId);
            return this;
        }

        public ColladaMapper putAnimation(String animationId, ItemState itemState) {
            animations.put(animationId, itemState);
            return this;
        }

        @Override
        public Integer getTextureId(String materialId) {
            return textures.get(materialId);
        }

        @Override
        public ItemState getItemState(String animationId) {
            return animations.get(animationId);
        }
    }

}
