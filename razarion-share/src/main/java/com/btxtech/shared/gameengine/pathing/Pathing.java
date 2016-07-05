package com.btxtech.shared.gameengine.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line2I;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class Pathing {
    private static final int PER_S = 10;
    public static final double FACTOR = 1.0 / (double) PER_S;
    public static final int MILLI_S = 1000 / PER_S;

    public static final double MAXIMUM_CORRECTION = 0.2;
    public static final double PENETRATION_TOLERANCE = 1;
    private final List<Unit> units = new ArrayList<>();
    // private List<UnitDataHolder> unitsContext;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private long tickCount;

    public void stop() {
        System.out.println("--- dispose ---");
        synchronized (units) {
            units.clear();
        }
        obstacles.clear();
        tickCount = 0;
    }

    public Unit createUnit(int id, boolean canMove, double radius, DecimalPosition position, DecimalPosition velocity, DecimalPosition destination, DecimalPosition lastDestination) {
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

    public Unit createUnit(int id, boolean canMove, double radius, DecimalPosition position, DecimalPosition destination, DecimalPosition lastDestination) {
        return createUnit(id, canMove, radius, position, null, destination, lastDestination);
    }

    public Obstacle createObstacle(Line2I line) {
        Obstacle obstacle = new Obstacle(line);
        obstacles.add(obstacle);
        return obstacle;
    }

    public void addObstacle(Obstacle obstacle) {
        obstacles.add(obstacle);
    }

    public List<Unit> getUnits() {
        synchronized (units) {
            return new ArrayList<>(units);
        }
    }

    public void restore(Collection<Unit> units, long tickCount) {
        synchronized (this.units) {
            this.units.clear();
            this.units.addAll(units);
        }
        this.tickCount = tickCount;
        ;
    }

    public List<Obstacle> getObstacles() {
        synchronized (obstacles) {
            return new ArrayList<>(obstacles);
        }
    }

    public void tick(double factor) {
        // saveContext();
        synchronized (units) {
            for (Unit unit : units) {
                unit.setupForTick(units, tickCount);
            }
        }
        Collection<Contact> contacts = findContacts();
        //dumpContacts(contacts);
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
//        if (runningCount > 0) {
//            System.out.println(tickCount + ":" + runningCount + " ------------------------------------ --");
//        }
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
//        System.out.println(" pathing.tick(XXX.FACTOR);");
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
                DebugHelper debugHelper1 = new DebugHelper("solveVelocityContacts ul", unit1, false);
                Unit unit2 = contact.getUnit2();
                // DebugHelper debugHelper2 = new DebugHelper("u2", unit2, false);
                debugHelper1.append("other unit", unit2);
                double newPenetration = calculateNewPenetration(unit1, unit2);
                debugHelper1.append("np", newPenetration);
                if (newPenetration == 0) {
                    debugHelper1.append("Not needed");
                    debugHelper1.dump();
                    //debugHelper2.dump();
                    continue;
                }
                DecimalPosition relativeVelocity = unit1.getVelocity();
                if (unit2.getVelocity() != null) {
                    relativeVelocity = relativeVelocity.sub(unit2.getVelocity());
                }
                debugHelper1.append("rv", relativeVelocity);
                double projection = contact.getNormal().dotProduct(relativeVelocity);
                debugHelper1.append("p", projection);
                DecimalPosition pushAway = contact.getNormal().multiply(-projection / 2.0);
                debugHelper1.append("pwy", pushAway);
                DecimalPosition newVelocity1 = unit1.getVelocity().add(pushAway);
                unit1.setVelocity(newVelocity1);
                debugHelper1.append("nv", newVelocity1);
                DecimalPosition velocity2 = unit2.getVelocity();
                if (velocity2 == null) {
                    velocity2 = new DecimalPosition(0, 0);
                }
                DecimalPosition newVelocity2 = velocity2.add(pushAway.multiply(-1));
                unit2.setVelocity(newVelocity2);
                debugHelper1.append("unit2 nv", newVelocity2);
                debugHelper1.dump();
            } else {
                DebugHelper debugHelper = new DebugHelper("solveVelocityContacts ol", unit1, false);
                if (contact.getUnit2() != null) {
                    debugHelper.append("unit", contact.getUnit2());
                } else {
                    debugHelper.append("obstacle", contact.getObstacle());
                }
                DecimalPosition velocity = unit1.getVelocity();
                double projection = contact.getNormal().dotProduct(velocity);
                debugHelper.append("projection", projection);
                if(projection >= 0) {
                    debugHelper.append("doo nothing");
                } else {
                    DecimalPosition pushAway = contact.getNormal().multiply(-projection);
                    debugHelper.append("pushAway", pushAway);
                    DecimalPosition newVelocity = velocity.add(pushAway);
                    debugHelper.append("newVelocity", newVelocity);
                    unit1.setVelocity(newVelocity);
                }
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
                    DecimalPosition pushAway = unit1.getPosition().sub(unit2.getPosition()).normalize(penetration / 2.0);
                    unit1.addToCenter(pushAway, tickCount);
                    unit2.addToCenter(pushAway.multiply(-1.0), tickCount);
                } else {
                    DecimalPosition pushAway = unit1.getPosition().sub(unit2.getPosition()).normalize(penetration);
                    unit1.addToCenter(pushAway, tickCount);
                }
            }
        }
        // obstacles
        synchronized (units) {
            for (Unit unit : units) {
                for (Obstacle obstacle : obstacles) {
                    // There is no check if the unit is inside the restricted area
                    DecimalPosition projection = obstacle.project(unit.getPosition());
                    double distance = projection.getDistance(unit.getPosition()) - unit.getRadius();
                    if (distance >= -PENETRATION_TOLERANCE) {
                        continue;
                    }
                    solved = false;
                    double penetration = -distance;
                    if (penetration > MAXIMUM_CORRECTION) {
                        penetration = MAXIMUM_CORRECTION;
                    }
                    DecimalPosition pushAway = unit.getPosition().sub(projection).normalize(penetration);
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
        double distance = unit1.getDesiredPosition().getDistance(unit2.getDesiredPosition()) - unit1.getRadius() - unit2.getRadius();
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

    public long getTickCount() {
        return tickCount;
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

    public Unit getUnit(DecimalPosition position) {
        for (Unit unit : getUnits()) {
            if (unit.isInside(position)) {
                return unit;
            }
        }
        return null;
    }

    public void removeAllUnits() {
        synchronized (units) {
            units.clear();
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





