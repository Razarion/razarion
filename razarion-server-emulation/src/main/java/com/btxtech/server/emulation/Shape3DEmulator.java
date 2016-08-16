package com.btxtech.server.emulation;

import com.btxtech.servercommon.collada.ColladaConverter;
import com.btxtech.servercommon.collada.ColladaConverterMapper;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemState;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Singleton
public class Shape3DEmulator {
    public List<Shape3D> getShape3Ds() {
        List<Shape3D> shape3Ds = new ArrayList<>();

        EmulatorColladaMapper mapper = new EmulatorColladaMapper();
        mapper.putTexture("Chassis_Material-material", 174014);
        mapper.putTexture("Wheel1Material-material", 174014);
        mapper.putTexture("Wheel2Material-material", 174014);
        mapper.putTexture("Wheel3Material-material", 174014);
        mapper.putTexture("Wheel4Material-material", 174014);
        shape3Ds.add(loadAndConvertShape3d(1, "C:\\dev\\projects\\razarion\\code\\tmp\\ItemType1Mirror.dae", mapper));
        mapper = new EmulatorColladaMapper();
        mapper.putTexture("Beam_Material-material", 174014);
        mapper.putTexture("SphereMaterial-material", 174014);
        mapper.putAnimation("Sphere_scale_X", ItemState.BEAM_UP).putAnimation("Sphere_scale_Y", ItemState.BEAM_UP).putAnimation("Sphere_scale_Z", ItemState.BEAM_UP);
        mapper.putAnimation("Plane_location_X", ItemState.BEAM_UP).putAnimation("Plane_location_Y", ItemState.BEAM_UP).putAnimation("Plane_location_Z", ItemState.BEAM_UP);
        shape3Ds.add(loadAndConvertShape3d(2, "C:\\dev\\projects\\razarion\\code\\tmp\\ArrivelBall01.dae", mapper));

        return shape3Ds;
    }


    private Shape3D loadAndConvertShape3d(int id, String fileName, ColladaConverterMapper colladaConverterMapper) {
        try {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
                String colladaString = buffer.lines().collect(Collectors.joining());
                return ColladaConverter.convertShape3D(colladaString, colladaConverterMapper).setDbId(id).setInternalName(fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class EmulatorColladaMapper implements ColladaConverterMapper {
        private Map<String, Integer> textures = new HashMap<>();
        private Map<String, ItemState> animations = new HashMap<>();

        public EmulatorColladaMapper putTexture(String animationId, Integer imageId) {
            textures.put(animationId, imageId);
            return this;
        }

        public EmulatorColladaMapper putAnimation(String animationId, ItemState itemState) {
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
