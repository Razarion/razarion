package com.btxtech.shared.system.debugtool;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Beat
 * on 16.11.2017.
 */
public class DebugHelperStatic {
    private static final String NULL_STRING = "null";
    private static List<DecimalPosition> polygon;
    private static List<DecimalPosition> positions;
    private static Integer FROM_TICK;
    private static Integer TO_TICK;
    private static Integer currentTick;
    private static String printOnTickMessage;

    public static void addPolygonPosition(DecimalPosition position) {
        if (polygon == null) {
            polygon = new ArrayList<>();
        }
        polygon.add(position);
    }

    public static List<DecimalPosition> getPolygon() {
        return polygon;
    }

    public static void addPosition(DecimalPosition position) {
        if (positions == null) {
            positions = new ArrayList<>();
        }
        positions.add(position);
    }

    public static List<DecimalPosition> getPositions() {
        return positions;
    }

    public static void add2printOnTick(String message) {
        if (omitTick()) {
            return;
        }
        if (printOnTickMessage == null) {
            printOnTickMessage = message;
        } else {
            printOnTickMessage += message;
        }
    }

    public static void addOrcaCreate(SyncPhysicalMovable syncPhysicalMovable) {
        if (syncPhysicalMovable.getSyncItem() == null) {
            return;
        }
        add2printOnTick("\n---------- Create ORCA:" + syncPhysicalMovable.getSyncItem().getId());
        String stringBuilder = "\nSyncPhysicalMovable syncPhysicalMovable" +
                syncPhysicalMovable.getSyncItem().getId() +
                " = GameTestHelper.createSyncPhysicalMovable(" +
                syncPhysicalMovable.getRadius() +
                ", TerrainType." +
                syncPhysicalMovable.getTerrainType() +
                ", " +
                generate(syncPhysicalMovable.getPosition2d()) +
                ", " +
                generate(syncPhysicalMovable.getVelocity()) +
                ", " +
                generate(syncPhysicalMovable.getPreferredVelocity()) +
                ", " +
                syncPhysicalMovable.getMaxSpeed() +
                ");";
        add2printOnTick(stringBuilder);
        add2printOnTick("\nOrca orca = new Orca(syncPhysicalMovable" + syncPhysicalMovable.getSyncItem().getId() + ");");

    }

    public static void addOrcaAdd(SyncPhysicalMovable syncPhysicalMovable) {
        if (syncPhysicalMovable.getSyncItem() == null) {
            return;
        }
        String stringBuilder = "\nSyncPhysicalMovable syncPhysicalMovable" +
                syncPhysicalMovable.getSyncItem().getId() +
                " = GameTestHelper.createSyncPhysicalMovable(" +
                syncPhysicalMovable.getRadius() +
                ", TerrainType." +
                syncPhysicalMovable.getTerrainType() +
                ", " +
                generate(syncPhysicalMovable.getPosition2d()) +
                ", " +
                generate(syncPhysicalMovable.getVelocity()) +
                ", " +
                generate(syncPhysicalMovable.getPreferredVelocity()) +
                ", " +
                syncPhysicalMovable.getMaxSpeed() +
                ");";
        add2printOnTick(stringBuilder);
        add2printOnTick("\norca.add(syncPhysicalMovable" + syncPhysicalMovable.getSyncItem().getId() + ");");
    }

    public static void addOrcaAdd(ObstacleSlope obstacleSlope) {
        NativeObstacle nativeObstacle = obstacleSlope.toNativeObstacle();
        add2printOnTick("\nobstacles.add(GameTestHelper.createObstacleSlope("
                + generate(new DecimalPosition(nativeObstacle.x1, nativeObstacle.y1))
                + ", " + generate(new DecimalPosition(nativeObstacle.x2, nativeObstacle.y2))
                + ", " + generate(new DecimalPosition(nativeObstacle.pDx, nativeObstacle.pDy))
                + ", " + nativeObstacle.p1C
                + ", " + generate(new DecimalPosition(nativeObstacle.p1Dx, nativeObstacle.p1Dy))
                + ", " + nativeObstacle.p2C
                + ", " + generate(new DecimalPosition(nativeObstacle.p2Dx, nativeObstacle.p2Dy))
                + "));");
    }

    public static void printAfterTick() {
        if (omitTick()) {
            return;
        }
        if (printOnTickMessage != null) {
            System.out.println("\n--------- Tick " + currentTick + ": " + printOnTickMessage);
            printOnTickMessage = null;
        }
    }

    public static boolean omitTick() {
        if (currentTick == null) {
            return true;
        }
        if (FROM_TICK != null && TO_TICK != null) {
            return FROM_TICK > currentTick || TO_TICK < currentTick;
        } else if (FROM_TICK != null) {
            return FROM_TICK < currentTick;
        } else if (TO_TICK != null) {
            return TO_TICK > currentTick;
        } else {
            return false;
        }
    }

    public static void setCurrentTick(Integer currentTick) {
        DebugHelperStatic.currentTick = currentTick;
    }

    public static void setTick(Integer fromTick, Integer toTick) {
        DebugHelperStatic.FROM_TICK = fromTick;
        DebugHelperStatic.TO_TICK = toTick;
    }

    // Comes to early
    public static boolean isCurrentTick(int tick) {
        return currentTick == tick;
    }

    public static String generate(DecimalPosition decimalPosition) {
        if (decimalPosition != null) {
            // return "new DecimalPosition(" + String.format(Locale.US, "%.3f, %.3f", decimalPosition.getX(), decimalPosition.getY()) + ")";
            return "**** NOT SUPPORTED IN GWT RT ****";
        } else {
            return NULL_STRING;
        }
    }
}
