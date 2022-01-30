package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.asset.AssetConfig;
import com.btxtech.shared.datatypes.asset.MeshContainer;
import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.nativejs.NativeMatrixDto;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.WeldUiBaseIntegrationTest;
import com.btxtech.uiservice.cdimock.TestNativeMatrixFactory;
import com.btxtech.uiservice.cdimock.renderer.RenderTaskCollector;
import com.btxtech.uiservice.cdimock.renderer.VertexContainerRendererTaskMock;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.btxtech.shared.dto.FallbackConfig.BUILDER_ITEM_TYPE_ID;

public class MeshRenderTest extends WeldUiBaseIntegrationTest {
    private final Logger logger = Logger.getLogger(MeshRenderTest.class.getName());

    @Test
    public void test() {
        // Init
        ColdGameUiContext coldGameUiContext = FallbackConfig.coldGameUiControlConfig(null);
        coldGameUiContext.setMeshContainers(loadMeshContainers());
        List<Shape3D> shape3ds = new ArrayList<>(loadShape3Ds());
        shape3ds.add(new Shape3D().id(999111).element3Ds(Collections.singletonList(new Element3D().id("element-1").vertexContainers(Collections.emptyList())))); // TODO remove
        coldGameUiContext.setShape3Ds(shape3ds);
        setMeshContainerId(coldGameUiContext, BUILDER_ITEM_TYPE_ID, "Vehicle_11");
        setupUiEnvironment(coldGameUiContext);
        AlarmService alarmService = getWeldBean(AlarmService.class);
        alarmService.addListener(alarm -> logger.severe(alarm.toString()));
        alarmService.getAlarms().forEach(alarm -> logger.severe(alarm.toString()));
        GameUiControl gameUiControl = getWeldBean(GameUiControl.class);
        gameUiControl.setColdGameUiContext(coldGameUiContext);
        gameUiControl.init();

        getWeldBean(TerrainUiService.class).setLoaded();

        setCamera(274, 100, 0);

        RenderService renderService = getWeldBean(RenderService.class);
        renderService.setup();

        // Runtime
        NativeMatrixDto nativeMatrixDto = new NativeMatrixDto();
        NativeSyncBaseItemTickInfo info = new NativeSyncBaseItemTickInfo();
        info.id = 1;
        info.baseId = 21;
        info.itemTypeId = BUILDER_ITEM_TYPE_ID;
        info.x = 274;
        info.y = 100;
        info.z = 2;
        nativeMatrixDto.numbers = Matrix4.createTranslation(info.x, info.y, info.z).toArray();
        info.model = nativeMatrixDto;
        info.spawning = 1;
        info.health = 1;
        info.buildup = 1;

        NativeSyncBaseItemTickInfo[] nativeSyncBaseItemTickInfos = new NativeSyncBaseItemTickInfo[1];
        nativeSyncBaseItemTickInfos[0] = info;

        BaseItemUiService baseItemUiService = getWeldBean(BaseItemUiService.class);
        baseItemUiService.updateSyncBaseItems(nativeSyncBaseItemTickInfos);
        renderService.render();

        // Gather post render information
        RenderTaskCollector renderTaskCollector = getWeldBean(RenderTaskCollector.class);
        Map<String, List<Matrix4>> element3DIdModelMatrices = new HashMap<>();
        renderTaskCollector.getAbstractRenderTaskMocks().forEach(abstractRenderTaskMock -> {
            VertexContainerRendererTaskMock vertexContainerRendererTaskMock = (VertexContainerRendererTaskMock) abstractRenderTaskMock;
            final List<Matrix4> modelMatrices = element3DIdModelMatrices.computeIfAbsent(vertexContainerRendererTaskMock.getVertexContainer().getKey(), k -> new ArrayList<>());
            abstractRenderTaskMock.getModelMatricesSupplier().apply(0L).forEach(mM -> modelMatrices.add(((TestNativeMatrixFactory.TestNativeMatrix) mM.getModel()).getMatrix4()));
        });
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("C:\\dev\\projects\\razarion\\code\\threejs_razarion\\src\\razarion_generated\\mesh_container\\element3DId_modelMatrices.json"), element3DIdModelMatrices);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<MeshContainer> loadMeshContainers() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AssetConfig assetConfig = mapper.readValue(new File("C:\\dev\\projects\\razarion\\code\\threejs_razarion\\src\\razarion_generated\\mesh_container\\unityAssetConverterTestAssetConfig.json"),
                    AssetConfig.class);

            int meshContainerId = 0;
            for (MeshContainer meshContainer : assetConfig.getMeshContainers()) {
                meshContainer.setId(meshContainerId++);
            }
            return assetConfig.getMeshContainers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Shape3D> loadShape3Ds() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File("C:\\dev\\projects\\razarion\\code\\razarion\\razarion-ui-service\\src\\test\\resources\\shape3Ds.json"),
                    new TypeReference<List<Shape3D>>() {
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setMeshContainerId(ColdGameUiContext coldGameUiContext, int baseItemId, String meshContainerName) {
        int meshContainerId = coldGameUiContext.getMeshContainers().stream()
                .filter(meshContainer -> meshContainer.getInternalName().equalsIgnoreCase(meshContainerName))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .getId();

        coldGameUiContext.getStaticGameConfig().getBaseItemTypes().stream()
                .filter(baseItemType -> baseItemId == baseItemType.getId())
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .setMeshContainerId(meshContainerId);
    }
}
