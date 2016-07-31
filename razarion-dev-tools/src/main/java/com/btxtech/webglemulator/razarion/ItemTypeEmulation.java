package com.btxtech.webglemulator.razarion;

import com.btxtech.servercommon.collada.ColladaConverter;
import com.btxtech.servercommon.collada.ColladaConverterInput;
import com.btxtech.servercommon.collada.ColladaConverterMapper;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemState;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.MovableType;
import com.btxtech.shared.gameengine.datatypes.itemtype.SpawnItemType;
import org.apache.commons.io.IOUtils;

import javax.inject.Singleton;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
        SPAWN_BASE_ITEM_TYPE,
        SIMPLE_MOVABLE
    }

    public List<ItemType> createItemTypes() {
        List<ItemType> itemTypes = new ArrayList<>();
        SpawnItemType spawnItemType = createSpawnItemType();
        itemTypes.add(spawnItemType);
        itemTypes.add(createSimpleMovable(spawnItemType));
        return itemTypes;
    }

    public BaseItemType createSimpleMovable(SpawnItemType spawnItemType) {
        BaseItemType builder = (BaseItemType) new BaseItemType().setName("Builder Emulation").setId(Id.SIMPLE_MOVABLE.ordinal()).setTerrainType(TerrainType.LAND);
        builder.setVertexContainer(new VertexContainer().setVertices(Arrays.asList(new Vertex(0, 0, 0), new Vertex(20, 0, 0), new Vertex(20, 20, 0))));
        return builder.setMovableType(new MovableType().setSpeed(10)).setHealth(100).setSpawnItemType(spawnItemType).setRadius(50);
    }

    public SpawnItemType createSpawnItemType() {
        try {
            SpawnItemType spawnItemType = new SpawnItemType().setDuration(10);
            spawnItemType.setName("Spawn Base Item Type").setId(Id.SPAWN_BASE_ITEM_TYPE.ordinal());
            ColladaMapper mapper = new ColladaMapper();
            mapper.putAnimation("Sphere_scale_X", ItemState.BEAM_UP).putAnimation("Sphere_scale_Y", ItemState.BEAM_UP).putAnimation("Sphere_scale_Z", ItemState.BEAM_UP);
            mapper.putAnimation("Plane_location_X", ItemState.BEAM_UP).putAnimation("Plane_location_Y", ItemState.BEAM_UP).putAnimation("Plane_location_Z", ItemState.BEAM_UP);
            spawnItemType.setShape3D(loadAndConvertShape3d("C:\\dev\\projects\\razarion\\code\\tmp\\ArrivelBall01.dae", mapper));
            return spawnItemType;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Shape3D loadAndConvertShape3d(String fileName, ColladaConverterMapper colladaConverterMapper) {
        try {
            String colladaString = IOUtils.toString(new FileInputStream(fileName));
            return ColladaConverter.convertShape3D(colladaString, colladaConverterMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class ColladaMapper implements ColladaConverterMapper {
        private Map<String, ItemState> animations = new HashMap<>();

        public ColladaMapper putAnimation(String animationId, ItemState itemState) {
            animations.put(animationId, itemState);
            return this;
        }

        @Override
        public Integer getTextureId(String materialId) {
            return -1;
        }

        @Override
        public ItemState getItemState(String animationId) {
            return animations.get(animationId);
        }
    }

}
