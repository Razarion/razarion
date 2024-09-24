package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.cdimock.TestExceptionHandler;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.gui.AbstractTestGuiRenderer;
import com.btxtech.shared.gui.TestGuiDisplay;
import javafx.scene.paint.Color;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.btxtech.shared.TestHelper.assertDecimalPosition;

public class ItemVelocityCalculatorTest {
    @Test
    public void pushAway1_1() {
        List<SyncItemHelper> syncItemHelpers = Arrays.asList(
                new SyncItemHelper().syncItemId(1).baseItemType(11).position(new DecimalPosition(20, 20)).velocity(new DecimalPosition(10, 0)).wayPositions(Collections.singletonList(new DecimalPosition(40, 20))),
                new SyncItemHelper().syncItemId(2).baseItemType(11).position(new DecimalPosition(24.5, 20))
        );
        List<SyncPhysicalMovable> syncPhysicalMovables = calculateVelocity(syncItemHelpers);
        assertItem(syncPhysicalMovables, 1, new DecimalPosition(10.5, 0));
        assertItem(syncPhysicalMovables, 2, new DecimalPosition(10.5, 0));
    }

    @Test
    public void pushAway1_2() {
        List<SyncItemHelper> syncItemHelpers = Arrays.asList(
                new SyncItemHelper().syncItemId(1).baseItemType(11).position(new DecimalPosition(20, 20)).velocity(new DecimalPosition(10, 0)).wayPositions(Collections.singletonList(new DecimalPosition(40, 20))),
                new SyncItemHelper().syncItemId(2).baseItemType(11).position(new DecimalPosition(24, 20))
        );
        List<SyncPhysicalMovable> syncPhysicalMovables = calculateVelocity(syncItemHelpers);
        assertItem(syncPhysicalMovables, 1, new DecimalPosition(10.5, 0));
        assertItem(syncPhysicalMovables, 2, new DecimalPosition(10.5, 0));
    }

    @Test
    public void pushAway2_1() {
        List<SyncItemHelper> syncItemHelpers = Arrays.asList(
                new SyncItemHelper().syncItemId(1).baseItemType(11).position(new DecimalPosition(20, 20)).velocity(new DecimalPosition(10, 0)).wayPositions(Collections.singletonList(new DecimalPosition(40, 20))),
                new SyncItemHelper().syncItemId(2).baseItemType(11).position(new DecimalPosition(20, 24)).velocity(new DecimalPosition(10, 0)).wayPositions(Collections.singletonList(new DecimalPosition(40, 24))),
                new SyncItemHelper().syncItemId(3).baseItemType(11).position(new DecimalPosition(23.5, 22))
        );
        List<SyncPhysicalMovable> syncPhysicalMovables = calculateVelocity(syncItemHelpers);
        assertItem(syncPhysicalMovables, 1, new DecimalPosition(10.5, 0));
        assertItem(syncPhysicalMovables, 2, new DecimalPosition( 9.817,3.724));
        assertItem(syncPhysicalMovables, 3, new DecimalPosition(7.915, 4.523));
    }

    private void assertItem(List<SyncPhysicalMovable> actuals, int syncItemId, DecimalPosition expectedVelocity) {
        SyncPhysicalMovable actual = actuals.stream().filter(syncPhysicalMovable -> syncPhysicalMovable.getSyncItem().getId() == syncItemId).findFirst().orElseThrow(IllegalArgumentException::new);
        assertDecimalPosition("Velocity is wrong", expectedVelocity, actual.getVelocity());
    }

    private List<SyncPhysicalMovable> calculateVelocity(List<SyncItemHelper> syncItemHelpers) {
        List<SyncPhysicalMovable> syncPhysicalMovables = new ArrayList<>();
        SyncItemContainerService syncItemContainerService = (center, radius, callback) -> syncPhysicalMovables.forEach(syncPhysicalMovable -> {
            if (syncPhysicalMovable.getPosition().getDistance(center) <= radius) {
                callback.accept(syncPhysicalMovable.getSyncItem());
            }
        });
        TerrainShapeManager terrainServiceMock = EasyMock.createNiceMock(TerrainShapeManager.class);
        EasyMock.replay(terrainServiceMock);
        PathingAccess pathingAccess = new PathingAccess(terrainServiceMock);
        TestExceptionHandler testExceptionHandler = new TestExceptionHandler(null);
        ItemVelocityCalculator itemVelocityCalculator = new ItemVelocityCalculator(syncItemContainerService, pathingAccess, testExceptionHandler);

        syncItemHelpers.forEach(syncItemHelper -> {
            syncPhysicalMovables.add(GameTestHelper.createSyncPhysicalMovable(2.0, syncItemHelper.syncItemId, TerrainType.LAND, syncItemHelper.position, syncItemHelper.velocity, syncItemHelper.wayPositions));
        });

        syncPhysicalMovables.forEach(itemVelocityCalculator::analyse);
        itemVelocityCalculator.calculateVelocity();
        // display(syncPhysicalMovables);

        testExceptionHandler.failIfException();

        return syncPhysicalMovables;
    }

    @SuppressWarnings("unused")
    private void display(List<SyncPhysicalMovable> syncPhysicalMovables) {
        TestGuiDisplay.show(new AbstractTestGuiRenderer() {
            @Override
            protected void doRender() {
                syncPhysicalMovables.forEach(syncPhysicalMovable -> {
                    strokeSyncPhysicalMovable(syncPhysicalMovable, 0.2, Color.GREEN);
                    if (syncPhysicalMovable.getVelocity() != null && !syncPhysicalMovable.getVelocity().equalsDeltaZero()) {
                        strokeLine(new Line(DecimalPosition.NULL, syncPhysicalMovable.getVelocity()), 0.2, Color.DARKBLUE);
                        strokeLine(new Line(syncPhysicalMovable.getPosition(), syncPhysicalMovable.getPosition().add(syncPhysicalMovable.getVelocity())), 0.2, Color.DARKBLUE);
                    }
                });
            }
        });

    }

    private static class SyncItemHelper {
        int syncItemId;
        int baseItemType;
        DecimalPosition position;
        double angle;
        DecimalPosition velocity;
        List<DecimalPosition> wayPositions;

        public SyncItemHelper syncItemId(int syncItemId) {
            this.syncItemId = syncItemId;
            return this;
        }

        public SyncItemHelper baseItemType(int baseItemType) {
            this.baseItemType = baseItemType;
            return this;
        }

        public SyncItemHelper position(DecimalPosition position) {
            this.position = position;
            return this;
        }

        public SyncItemHelper angle(double angle) {
            this.angle = angle;
            return this;
        }

        public SyncItemHelper velocity(DecimalPosition velocity) {
            this.velocity = velocity;
            return this;
        }

        public SyncItemHelper wayPositions(List<DecimalPosition> wayPositions) {
            this.wayPositions = wayPositions;
            return this;
        }
    }

}