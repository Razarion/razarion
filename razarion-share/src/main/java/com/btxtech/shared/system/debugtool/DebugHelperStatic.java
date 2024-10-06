package com.btxtech.shared.system.debugtool;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeObstacle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 16.11.2017.
 */
public class DebugHelperStatic {
    private static final String TICK_DATA_PATH = "C:\\dev\\projects\\razarion\\code\\razarion";
    public static final String TICK_DATA_SLAVE = TICK_DATA_PATH + "\\slave.json";
    public static final String TICK_DATA_MASTER = TICK_DATA_PATH + "\\master.json";
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


    public static void appendAfterTick(List<DebugHelperStatic.TickData> tickDatas, double tickCount, SyncItemContainerServiceImpl syncItemContainerService) {
        TickData tickData = new TickData();
        tickData.setDate(new Date());
        tickData.setTickCount(tickCount);
        syncItemContainerService.iterateOverBaseItemsIdOrdered(tickData::addSyncBaseItem);
        tickDatas.add(tickData);
    }

    public static void clearTickDatas(List<DebugHelperStatic.TickData> tickDatas) {
        tickDatas.clear();
    }

    public static void add2printOnTick(String message) {
        System.out.println(message);
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
                generate(syncPhysicalMovable.getPosition()) +
                ", " +
                generate(syncPhysicalMovable.getVelocity()) +
                ", " +
                generate(syncPhysicalMovable.getPreferredVelocity()) +
                ", " +
                syncPhysicalMovable.getMaxSpeed() +
                ");";
        add2printOnTick(stringBuilder);
    }

    public static void addOrcaAdd(SyncPhysicalMovable syncPhysicalMovable) {
        if (syncPhysicalMovable.getSyncItem() == null) {
            return;
        }
        String stringBuilder = "GameTestHelper.createSyncPhysicalMovable(" +
                syncPhysicalMovable.getRadius() +
                ", TerrainType." +
                syncPhysicalMovable.getTerrainType() +
                ", " +
                generate(syncPhysicalMovable.getPosition()) +
                ", " +
                generate(syncPhysicalMovable.getPreferredVelocity()) +
                ")";
        add2printOnTick("physicalAreas.add(" + stringBuilder + ");");
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

    public static void printAfterTick(DebugHelper debugHelper) {
        if (omitTick()) {
            return;
        }
        if (printOnTickMessage != null) {
            if (debugHelper != null) {
                debugHelper.debugToConsole(printOnTickMessage);
            } else {
                System.out.println("\n--------- Tick " + currentTick + ": " + printOnTickMessage);
            }
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
        return currentTick != null && currentTick == tick;
    }

    public static String generate(DecimalPosition decimalPosition) {
        if (decimalPosition != null) {
            // return "new DecimalPosition(" + String.format(Locale.US, "%.3f, %.3f", decimalPosition.getX(), decimalPosition.getY()) + ")";
            return "new DecimalPosition(" + decimalPosition.getX() + ", " + decimalPosition.getY() + ")";
            // return "**** NOT SUPPORTED IN GWT RT ****";
        } else {
            return NULL_STRING;
        }
    }

    public static class TickData {
        private double tickCount; // TODO should be called tick-number
        private Date date;
        private List<TickSyncBaseItem> tickSyncBaseItems;

        public double getTickCount() {
            return tickCount;
        }

        public void setTickCount(double tickCount) {
            this.tickCount = tickCount;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public List<TickSyncBaseItem> getTickSyncBaseItems() {
            return tickSyncBaseItems;
        }

        public void setTickSyncBaseItems(List<TickSyncBaseItem> tickSyncBaseItems) {
            this.tickSyncBaseItems = tickSyncBaseItems;
        }

        public void addSyncBaseItem(SyncBaseItem syncBaseItem) {
            if (!syncBaseItem.getAbstractSyncPhysical().canMove()) {
                return;
            }
            if (tickSyncBaseItems == null) {
                tickSyncBaseItems = new ArrayList<>();
            }
            TickSyncBaseItem tickSyncBaseItem = new TickSyncBaseItem();
            tickSyncBaseItem.setId(syncBaseItem.getId());
            tickSyncBaseItem.setPosition(syncBaseItem.getAbstractSyncPhysical().getPosition());
            tickSyncBaseItem.setVelocity(syncBaseItem.getSyncPhysicalMovable().getVelocity());
            tickSyncBaseItem.setAngle(syncBaseItem.getAbstractSyncPhysical().getAngle());
            tickSyncBaseItem.setRadius(syncBaseItem.getAbstractSyncPhysical().getRadius());
            if (syncBaseItem.getSyncPhysicalMovable().getPath() != null) {
                tickSyncBaseItem.setPath(syncBaseItem.getSyncPhysicalMovable().getPath().getWayPositions());
            }
            tickSyncBaseItem.setPreferredVelocity(syncBaseItem.getSyncPhysicalMovable().getPreferredVelocity());
            tickSyncBaseItems.add(tickSyncBaseItem);
        }
    }

    public static class TickSyncBaseItem {
        private int id;
        private DecimalPosition position;
        private DecimalPosition velocity;
        private double angle;
        private double radius;
        private List<DecimalPosition> path;
        private DecimalPosition preferredVelocity;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public DecimalPosition getPosition() {
            return position;
        }

        public void setPosition(DecimalPosition position) {
            this.position = position;
        }

        public DecimalPosition getVelocity() {
            return velocity;
        }

        public void setVelocity(DecimalPosition velocity) {
            this.velocity = velocity;
        }

        public double getAngle() {
            return angle;
        }

        public void setAngle(double angle) {
            this.angle = angle;
        }

        public double getRadius() {
            return radius;
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public List<DecimalPosition> getPath() {
            return path;
        }

        public void setPath(List<DecimalPosition> path) {
            this.path = path;
        }

        public DecimalPosition getPreferredVelocity() {
            return preferredVelocity;
        }

        public void setPreferredVelocity(DecimalPosition preferredVelocity) {
            this.preferredVelocity = preferredVelocity;
        }
    }
}
