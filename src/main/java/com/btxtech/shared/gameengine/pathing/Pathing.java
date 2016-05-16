package com.btxtech.shared.gameengine.pathing;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Pathing {
    private static final int PER_S = 10;
    public static final double FACTOR = 1.0 / (double) PER_S;
    public static final int MILLI_S = 1000 / PER_S;

    public static final Integer DEBUG_ALL_FILTER = null;
    public static final Integer DEBUG_SELECTIVE_FILTER = null;
    public static final double MAXIMUM_CORRECTION = 0.2;
    public static final double PENETRATION_TOLERANCE = 1;
    private final List<Unit> units = new ArrayList<>();
    // private List<UnitDataHolder> unitsContext;
    private List<Obstacle> obstacles = new ArrayList<>();
    private long tickCount;

    public void stop() {
        System.out.println("--- dispose ---");
        synchronized (units) {
            units.clear();
        }
        obstacles.clear();
        tickCount = 0;
    }

    public Unit createUnit(int id, boolean canMove, double radius, Vector2 position, Vector2 velocity, Vector2 destination, Vector2 lastDestination) {
        if (velocity != null && !canMove) {
            throw new IllegalArgumentException();
        }
        Unit unit = new Unit(id, canMove, radius, position, destination, lastDestination, tickCount);
        unit.setVelocity(velocity);
        synchronized (units) {
            units.add(unit);
        }
        return unit;
    }

    public Unit createUnit(int id, boolean canMove, double radius, Vector2 position, Vector2 destination, Vector2 lastDestination) {
        return createUnit(id, canMove, radius, position, null, destination, lastDestination);
    }

    public Obstacle createObstacle(double x, double y, double width, double height) {
        Obstacle obstacle = new Obstacle(x, y, width, height);
        obstacles.add(obstacle);
        return obstacle;
    }

    public List<Unit> getUnits() {
        List<Unit> units = new ArrayList<>();
        synchronized (this.units) {
            for (Unit unit : this.units) {
                units.add(unit);
            }
        }
        return units;
    }

    public List<Obstacle> getObstacles() {
        List<Obstacle> iObstacles = new ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            iObstacles.add(obstacle);
        }
        return iObstacles;
    }

    public void loop(double factor) {
        // saveContext();
        synchronized (units) {
            for (Unit unit : units) {
                unit.setupForTick(units, tickCount);
            }
        }
        Collection<Contact> contacts = findContacts();
        // dumpContacts(contacts);
        for (int i = 0; i < 10; i++) {
            solveVelocityContacts(contacts);
        }
        implementPosition(tickCount);
        while (!solvePositionContacts(tickCount)) ;
        int runningCount = 0;
        synchronized (units) {
            for (Unit unit : units) {
                if (unit.hasDestination()) {
                    runningCount++;
                    if (unit.checkDestinationReached(units)) {
                        unit.stop();
                    }
                }
            }
        }
        if (runningCount > 0) {
            System.out.println(tickCount + ":" + runningCount + " ------------------------------------ --");
        }
        tickCount++;
    }

    private Collection<Unit> getUnits(Collection<Contact> contacts, Unit unit) {
        Collection<Unit> contactingUnits = new ArrayList<>();
        for (Contact contact : contacts) {
            if (unit.equals(contact.getUnit1()) && contact.getUnit2() != null) {
                contactingUnits.add(contact.getUnit2());
            }
            if (unit.equals(contact.getUnit2())) {
                contactingUnits.add(contact.getUnit1());
            }
        }
        return contactingUnits;
    }

//    public void printTestCase() {
//        System.out.println(" @Test");
//        System.out.println(" public void test() {");
//        System.out.println(" Pathing pathing = new Pathing();");
//        for (UnitDataHolder unitDataHolder : unitsContext) {
//            System.out.println(" pathing.createUnit(" +
//                    unitDataHolder.testString() + ") ; ");
//        }
//        for (Obstacle obstacle : obstacles) {
//            System.out.println(" pathing.createObstacle(" +
//                    obstacle.testString() + ");");
//        }
//        System.out.println(" pathing.loop(XXX.FACTOR);");
//        System.out.println(" }");
//    }
//
//    private void saveContext() {
//        unitsContext = new ArrayList<>();
//        synchronized (units) {
//            for (Unit unit : units) {
//                unitsContext.add(new UnitDataHolder(unit));
//            }
//        }
//    }

    private void dumpContacts(Collection<Contact> contacts) {
        if (contacts.isEmpty()) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        stringBuilder.append(contacts.size());
        stringBuilder.append("):");
        for (Contact contact : contacts) {
            stringBuilder.append(contact.getUnit1().getId());
            stringBuilder.append("-");
            if (contact.getUnit2() != null) {
                stringBuilder.append(contact.getUnit2().getId());
            } else {
                stringBuilder.append("|");
            }
            stringBuilder.append(":");
        }
        System.out.println(stringBuilder);
    }

    private Collection<Contact> findContacts() {
        Collection<Contact> contacts = new ArrayList<>();
        Collection<Unit> alreadyAddedUnits = new ArrayList<>();
        synchronized (units) {
            for (Unit unit : units) {
                if (unit.isMoving()) {
                    findObstacleContacts(unit, contacts);
                    findUnitContacts(unit, alreadyAddedUnits, contacts);
                }
                alreadyAddedUnits.add(unit);
            }
        }
        return contacts;
    }

    private void findObstacleContacts(Unit unit, Collection<Contact> contacts) {
        for (Obstacle obstacle : obstacles) {
            Contact contact = obstacle.hasContact(unit);
            if (contact != null) {
                contacts.add(contact);
            }
        }
    }

    private void findUnitContacts(Unit unit, Collection<Unit> alreadyAddedUnits, Collection<Contact> contacts) {
        synchronized (units) {
            for (Unit other : units) {
                if (unit == other) {
                    continue;
                }
                if (alreadyAddedUnits.contains(other)) {
                    continue;
                }
                Contact contact = unit.hasContact(other);
                if (contact != null) {
                    contacts.add(contact);
                }
            }
        }
    }

    private void solveVelocityContacts(Collection<Contact> contacts) {
        for (Contact contact : contacts) {
            Unit unit1 = contact.getUnit1();
            if (contact.hasUnit2AndCanMove()) {
                DebugHelper debugHelper1 = new DebugHelper("ul", unit1, false);
                Unit unit2 = contact.getUnit2();
                DebugHelper debugHelper2 = new DebugHelper("u2", unit2, false);
                double newPenetration = calculateNewPenetration(unit1, unit2);
                debugHelper1.append("np", newPenetration);
                if (newPenetration == 0) {
                    debugHelper1.append("Not needed");
                    debugHelper1.dump();
                    debugHelper2.dump();
                    continue;
                }
                Vector2 relativeVelocity = unit1.getVelocity();
                if (unit2.getVelocity() != null) {
                    relativeVelocity = relativeVelocity.difference(unit2.getVelocity());
                }
                debugHelper1.append("rv", relativeVelocity);
                double projection = contact.getNormal().dot(relativeVelocity);
                debugHelper1.append("p", projection);
                Vector2 pushAway = contact.getNormal().product(-projection / 2.0);
                debugHelper1.append("pwy", pushAway);
                Vector2 newVelocity1 = unit1.getVelocity().sum(pushAway);
                unit1.setVelocity(newVelocity1);
                debugHelper1.append("nv", newVelocity1);
                Vector2 velocity2 = unit2.getVelocity();
                if (velocity2 == null) {
                    velocity2 = new Vector2(0, 0);
                }
                Vector2 newVelocity2 = velocity2.sum(pushAway.product(-1));
                unit2.setVelocity(newVelocity2);
                debugHelper2.append("nv", newVelocity2);
                debugHelper1.dump();
                debugHelper2.dump();
            } else {
                DebugHelper debugHelper = new DebugHelper("ol", unit1, false);
                Vector2 velocity = unit1.getVelocity();
                double projection = contact.getNormal().dot(velocity);
                debugHelper.append("p", projection);
                Vector2 pushAway = contact.getNormal().product(-projection);
                debugHelper.append("pwy", pushAway);
                Vector2 newVelocity = velocity.sum(pushAway);
                debugHelper.append("nv", newVelocity);
                unit1.setVelocity(newVelocity);
                debugHelper.dump();
            }
        }
    }

    private boolean solvePositionContacts(long tickCount) {
        boolean solved = true;
        // Units
        List<Unit> unitsToCheck;
        synchronized (units) {
            unitsToCheck = new ArrayList<>(units);
        }
        while (!unitsToCheck.isEmpty()) {
            Unit unit1 = unitsToCheck.remove(0);
            for (Unit unit2 : unitsToCheck) {
                double distance = unit1.getDistance(unit2);
                if (distance >= -PENETRATION_TOLERANCE) {
                    continue;
                }
                solved = false;
                double penetration = -distance;
                if (penetration > MAXIMUM_CORRECTION) {
                    penetration = MAXIMUM_CORRECTION;
                }
                if (unit2.isCanMove()) {
                    Vector2 pushAway = unit1.getCenter().difference(unit2.getCenter()).setMagnitude(penetration / 2.0);
                    unit1.addToCenter(pushAway, tickCount);
                    unit2.addToCenter(pushAway.product(-1.0), tickCount);
                } else {
                    Vector2 pushAway = unit1.getCenter().difference(unit2.getCenter()).setMagnitude(penetration);
                    unit1.addToCenter(pushAway, tickCount);
                }
            }
        }
        // obstacles
        synchronized (units) {
            for (Unit unit : units) {
                for (Obstacle obstacle : obstacles) {
                    if (obstacle.getRectangle().contains(unit.getCenter())) {
                        throw new IllegalStateException();
                    }
                    Vector2 projection = Pathing.projectOnRectangle(obstacle.getRectangle(), unit.getCenter());
                    double distance = projection.distance(unit.getCenter()) - unit.getRadius();
                    if (distance >= -PENETRATION_TOLERANCE) {
                        continue;
                    }
                    solved = false;
                    double penetration = -distance;
                    if (penetration > MAXIMUM_CORRECTION) {
                        penetration = MAXIMUM_CORRECTION;
                    }
                    Vector2 pushAway = unit.getCenter().difference(projection).setMagnitude(penetration);
                    unit.addToCenter(pushAway, tickCount);
                }
            }
        }
        return solved;
    }

    private double calculatePenetration(Contact contact) {
        double distance;
        if (contact.getUnit2() != null) {
            distance = contact.getUnit1().getDistance(contact.getUnit2());
        } else {
            distance = contact.getObstacle().getDistance(contact.getUnit1());
        }
        if (distance > 0) {
            return 0;
        } else {
            return -distance;
        }
    }

    private double calculateNewPenetration(Unit unit1, Unit unit2) {
        double distance = unit1.getDesiredPosition().distance(unit2.getDesiredPosition()) - unit1.getRadius() - unit2.getRadius();
        if (distance > 0) {
            return 0;
        }
        return -distance;
    }

    private void implementPosition(long tickCount) {
        synchronized (units) {
            for (Unit unit : units) {
                unit.implementPosition(tickCount);
            }
        }
    }

    public static Vector2 projectOnRectangle(Rectangle rectangle, Vector2 point) {
        Vector2 half1 = new Vector2(rectangle.getWidth() / 2.0, rectangle.getHeight() / 2.0);
        Vector2 half2 = new Vector2(rectangle.getWidth() / 2.0, -rectangle.getHeight() / 2.0);
        Vector2 cornerA = rectangle.getCenter().difference(half1);
        Vector2 cornerB = rectangle.getCenter().sum(half2);
        Vector2 cornerC = rectangle.getCenter().sum(half1);
        Vector2 cornerD = rectangle.getCenter().difference(half2);
        Line line1 = Pathing.createLine(cornerA, cornerB);
        Line line2 = Pathing.createLine(cornerB, cornerC);
        Line line3 = Pathing.createLine(cornerC, cornerD);
        Line line4 = Pathing.createLine(cornerD, cornerA);

        List<Vector2> projections = new ArrayList<>();

        Vector2D position = new Vector2D(point.x, point.y);
        // Check top line
        Vector2D projection = (Vector2D) line1.project(position);
        if (projection.getX() < cornerA.x) {
            projections.add(cornerA);
        } else if (projection.getX() > cornerB.x) {
            projections.add(cornerB);
        } else {
            projections.add(new Vector2(projection.getX(), projection.getY()));
        }
        // Check right line
        projection = (Vector2D) line2.project(position);
        if (projection.getY() < cornerB.y) {
            projections.add(cornerB);
        } else if (projection.getY() > cornerC.y) {
            projections.add(cornerC);
        } else {
            projections.add(new Vector2(projection.getX(), projection.getY()));
        }
        // Check bottom 1ine
        projection = (Vector2D) line3.project(position);
        if (projection.getX() < cornerD.x) {
            projections.add(cornerD);
        } else if (projection.getX() > cornerC.x) {
            projections.add(cornerC);
        } else {
            projections.add(new Vector2(projection.getX(), projection.getY()));
        }
        // Check left 1ine
        projection = (Vector2D) line4.project(position);
        if (projection.getY() < cornerA.y) {
            projections.add(cornerA);
        } else if (projection.getY() > cornerD.y) {
            projections.add(cornerD);
        } else {
            projections.add(new Vector2(projection.getX(), projection.getY()));
        }
        // Find nearest point
        double min = Double.MAX_VALUE;
        Vector2 nearestProjection = null;
        for (Vector2 projectionOnRect : projections) {
            double distance = point.distance(projectionOnRect);
            if (distance < min) {
                nearestProjection = projectionOnRect;
                min = distance;
            }
        }
        if (nearestProjection == null) {
            throw new IllegalArgumentException();
        }
        return nearestProjection;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static Vector2 rotate90Dec(Vector2 vector) {
        return new Vector2(-vector.y, vector.x);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static Vector2 rotateMinus90Dec(Vector2 vector) {
        return new Vector2(vector.y, -vector.x);
    }

    public static Line createLine(Vector2 start, Vector2 end) {
        return new Line(new Vector2D(start.x, start.y), new Vector2D(end.x, end.y), 0.1);
    }

    public static double correctAngle(double angle) {
        if (angle > Math.PI) {
            return angle - 2 * Math.PI;
        } else if (angle < -Math.PI) {
            return 2 * Math.PI + angle;
        } else {
            return angle;
        }
    }

    public static double deltaAngle(double angle1, double angle2) {
        angle1 = correctAngle(angle1);
        angle2 = correctAngle(angle2);
        if (angle1 < 0) {
            angle1 = angle1 + 2.0 * Math.PI;
        }
        if (angle2 < 0) {
            angle2 = angle2 + 2.0 * Math.PI;
        }
        return Math.abs(correctAngle(Math.abs(angle1 - angle2)));
    }

    public static String toVector2String(Vector2 vector) {
        if (vector != null) {
            return String.format("(%.2f:%.2f)", vector.x, vector.y);
        } else {
            return "(-:-)";
        }
    }

}

// TODO make two units dest in middle (both point to the destination before start)
// TODO 1 slow
// TODO 3: prevent lining up
// TODO 16,17,23 does not bypass enough
// TODO 18 tooks very 1ong unti1 are units are stopped
// TODO 31,32,33 very slow
// TODO Performance(solve position contacts & stop condition)
// TODO different speed, acceleration and radius





