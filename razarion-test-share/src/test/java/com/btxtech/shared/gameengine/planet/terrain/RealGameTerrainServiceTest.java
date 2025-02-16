package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import javafx.scene.paint.Color;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.btxtech.shared.RestClientHelper.readColdGameUiContext;

/**
 * Created by Beat
 * on 14.11.2017.
 */
@Ignore
public class RealGameTerrainServiceTest extends DaggerTerrainServiceTestBase {

    @Test
    public void readFromServer() throws IOException {
        System.out.println("Start Read ColdGameUiContext");
        ColdGameUiContext coldGameUiContext = readColdGameUiContext(117);
        System.out.println("Loaded ColdGameUiContext");
        System.out.println("Start Read TerrainEditorLoad");
        System.out.println("Loaded TerrainEditorLoad");

//        System.out.println("Write static-game-config.json");
//        String directory = "C:\\dev\\projects\\razarion\\code\\razarion\\razarion-share\\src\\test\\resources\\com\\btxtech\\shared\\gameengine\\planet\\terrain";
//        ObjectMapper objectMapper = new ObjectMapper();
//        SimpleModule module = new SimpleModule();
//        module.addSerializer(TestFloat32Array.class, new TestFloat32ArraySerializer());
//        objectMapper.registerModule(module);
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(directory, "static-game-config.json"), coldGameUiContext.getStaticGameConfig());


        setupTerrainTypeService(coldGameUiContext.getStaticGameConfig(), null, coldGameUiContext.getWarmGameUiContext().getPlanetConfig(), null, null);
        double radius = 1;
        final SingleHolder<DecimalPosition> actorPosition = new SingleHolder<>();
        DecimalPosition target = new DecimalPosition(104, 125.5);
        showDisplay(new MouseMoveCallback().setCallback(position -> {
            try {
                SimplePath simplePath = setupPath(position, radius, TerrainType.LAND, target);
                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(position, radius), Color.DARKGRAY), new PositionMarker().addCircleColor(new Circle2D(target, radius), Color.RED), simplePath};
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return new Object[]{new PositionMarker().addCircleColor(new Circle2D(position, radius), Color.DARKGRAY),new PositionMarker().addCircleColor(new Circle2D(target, radius), Color.BLUE)};
        }));
    }

    protected SimplePath setupPath(DecimalPosition target, double radius, TerrainType actorTerrainType, DecimalPosition actorPosition) {
        SyncBaseItem actor = GameTestHelper.createMockSyncBaseItem(radius, actorTerrainType, actorPosition, null);
        return getPathingService().setupPathToDestination(actor, target);
    }

}
