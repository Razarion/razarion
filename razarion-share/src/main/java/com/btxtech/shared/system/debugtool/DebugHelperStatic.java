package com.btxtech.shared.system.debugtool;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 16.11.2017.
 */
public class DebugHelperStatic {
    private static List<DecimalPosition> polygon;
    private static List<DecimalPosition> positions;
    private static final int FROM_TICK = 49;
    private static final int TO_TICK = 50;
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

    public static void printOnTick() {
        if (omitTick()) {
            return;
        }
        System.out.println("\n--------- Tick: " + currentTick + printOnTickMessage);
        printOnTickMessage = null;
    }

    private static boolean omitTick() {
        if (currentTick == null) {
            return true;
        }
        return FROM_TICK > currentTick || TO_TICK < currentTick;
    }

    public static void setCurrentTick(Integer currentTick) {
        DebugHelperStatic.currentTick = currentTick;
    }
}
