package com.btxtech.shared.gameengine.pathing;

import org.dyn4j.geometry.Vector2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 16.05.2016.
 */
public class Unit {
    public final static double SPEED = 40; // Pixel per second
    private final static double MIN_SPEED = 40.0 * 0.2; // Min speed if wrong angle pixel per second
    //private final static double ANGEL_MIN_SPEED = Math.toRadians(30);
    private final static double ANGULAR_VELOCITY = Math.toRadians(30); //Grad per second
    private final static double ACCELERATION = 40;
    private final static int LOOK_AHEAD_TICKS = 20;
    private final static double LOOK_AHEAD_DISTANCE = LOOK_AHEAD_TICKS * SPEED * Pathing.FACTOR;
    private final static double BYPASS_FACTOR = 2.0 * SPEED * Pathing.FACTOR;
    private final static long BYPASS_IGNORE_TICK_COUNT = 10;
    private final static double UNIT_DISTANCE_DESTINATION_CHECK = 2.0;
    private final int id;
    private final boolean canMove;
    private final double radius;
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 destination;
    private Vector2 lastDestination;
    private long lastTickPositionImproved;
    private double nearestDistance;
    private double angle;

    public Unit(int id, boolean canMove, double radius, Vector2 position, Vector2 destination, Vector2 lastDestination, long tickCount) {
        if (destination != null && !canMove) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.canMove = canMove;
        this.radius = radius;
        this.position = position;
        this.destination = destination;
        this.lastDestination = lastDestination;
        if (destination != null) {
            nearestDistance = position.distance(destination);
            lastTickPositionImproved = tickCount;
        }
    }

    public int getId() {
        return id;
    }

    public boolean isMarked() {
        return Pathing.DEBUG_SELECTIVE_FILTER != null && Pathing.DEBUG_SELECTIVE_FILTER == id;
    }

    public double getRadius() {
        return radius;
    }

    public Vector2 getCenter() {
        return position;
    }

    public void addToCenter(Vector2 delta, long tickCount) {
        position = position.sum(delta);
        updateNearestDistance(tickCount);
    }

    private void updateNearestDistance(long tickCount) {
        if (destination != null) {
            double newDistance = position.distance(destination);
            if (newDistance < nearestDistance) {
                nearestDistance = newDistance;
            }
            lastTickPositionImproved = tickCount;
        }
    }

    public Contact hasContact(Unit other) {
        double distance = getDistance(other);
        if (distance >= 0) {
            return null;
        }
        Vector2 norm = position.difference(other.getCenter());
        norm.normalize();
        return new Contact(this, other, norm);
    }

    public double getDistance(Unit other) {
        return position.distance(other.getCenter()) - radius - other.getRadius();
    }

    public void implementPosition(long tickCount) {
        DebugHelper debugHelper = new DebugHelper("imp1", this, false);
        position = getDesiredPosition();
        updateNearestDistance(tickCount);
        debugHelper.append("np", position);
        //debugHelper.append("time", system.currentTimeMillis());
        debugHelper.dump();
    }

    public Vector2 getDesiredPosition() {
        Vector2 desiredPosition = new Vector2(position);
        if (velocity != null) {
            desiredPosition =
                    desiredPosition.sum(velocity.product(Pathing.FACTOR));
        }
        return desiredPosition;
    }

    public boolean hasDestination() {
        return destination != null;
    }

    public Vector2 getDestination() {
        return destination;
    }

    public boolean isMoving() {
        return velocity != null && !velocity.isZero();
    }

    public double getAngle() {
        return angle;
    }

    public void stop() {
        lastDestination = destination;
        destination = null;
    }

    public void setupForTick(List<Unit> units, long tickCount) {
        DebugHelper debugHelper = new DebugHelper("setupForTick", this, false);
        if (destination != null) {
            Vector2 desiredVelocity = destination.difference(position);
            desiredVelocity.setMagnitude(SPEED);
            debugHelper.append("desired vl", desiredVelocity);
            if (velocity == null) {
                velocity = new Vector2(0.001, 0);
                velocity.setDirection(angle);
            }
            debugHelper.appendAngle("angle from v", velocity.getDirection());
            desiredVelocity = forwardLooking(desiredVelocity, units, tickCount);
            desiredVelocity.setMagnitude(SPEED);
            double desiredAngle = desiredVelocity.getDirection();
            debugHelper.append("desired v", desiredVelocity);
            debugHelper.appendAngle("desired a", desiredVelocity.getDirection());
            // double deltaAngle = velocity.getAngleBetween(desiredVelocity);
            double deltaAngle = Pathing.correctAngle(desiredVelocity.getDirection() - angle);
            debugHelper.appendAngle("deltaAngle", deltaAngle);
            // Fix angle
            Vector2 fixedVelocity = new Vector2(desiredVelocity);
            double angleSpeedFactor = 1.0;
            if (Math.abs(deltaAngle) > ANGULAR_VELOCITY * Pathing.FACTOR) {
                debugHelper.append("angle too big");
                double possibleAngle = Pathing.correctAngle(angle + Math.signum(deltaAngle) * ANGULAR_VELOCITY * Pathing.FACTOR);
                debugHelper.appendAngle("possibleAngle", possibleAngle);
                angle = possibleAngle;
                fixedVelocity.setDirection(possibleAngle);
                Vector2 desiredVelocityNorm = new Vector2(desiredVelocity);
                desiredVelocityNorm.normalize();
                Vector2 fixedAngleVelocityNorm = new Vector2(fixedVelocity);
                fixedAngleVelocityNorm.normalize();
                angleSpeedFactor = Math.max(0.0, Math.min(1.0, fixedAngleVelocityNorm.dot(desiredVelocityNorm)));
            } else {
                angle = desiredAngle;
                fixedVelocity.setDirection(desiredAngle);
            }
            debugHelper.appendAngle("angle", angle);
            // Fix velocity
            double originalSpeed = velocity.getMagnitude(); // TODO That is wrong
            debugHelper.append("originalSpeed", originalSpeed);
            double possibleSpeed = Math.max(MIN_SPEED, angleSpeedFactor * SPEED);
            debugHelper.append("possibleSpeed", possibleSpeed);
            double speed;
            if (Math.abs(originalSpeed - possibleSpeed) > ACCELERATION * Pathing.FACTOR) {
                if (originalSpeed < possibleSpeed) {
                    debugHelper.append("accelerate");
                    speed = originalSpeed + ACCELERATION * Pathing.FACTOR;
                } else {
                    debugHelper.append("s1ow down");
                    speed = originalSpeed - ACCELERATION * Pathing.FACTOR;
                }
            } else {
                debugHelper.append("speed ok");
                speed = possibleSpeed;
            }
            // Check if destination too near to turn
            deltaAngle = Pathing.correctAngle(desiredVelocity.getDirection() - angle);
            double turnSteps = Math.abs(deltaAngle) / (ANGULAR_VELOCITY * Pathing.FACTOR);
            double distance = turnSteps * speed * Pathing.FACTOR;
            if (distance > position.distance(destination)) {
                speed = originalSpeed - ACCELERATION * Pathing.FACTOR;
                debugHelper.append("turn angle too big");
            }
            speed = Math.min(SPEED, speed);
            speed = Math.max(0.0, speed);
            fixedVelocity.setMagnitude(speed);
            fixedVelocity.setDirection(angle);
            velocity = fixedVelocity;

            debugHelper.append("magnitude", velocity.getMagnitude());
        } else {
            if (velocity == null) {
                debugHelper.dump();
                return;
            }
            double magnitude = velocity.getMagnitude();
            double acceleration = ACCELERATION * Pathing.FACTOR;
            if (acceleration >= magnitude) {
                velocity = null;
            } else {
                velocity.setMagnitude(magnitude - acceleration);
                if (velocity.isZero()) {
                    velocity = null;
                }
            }
        }
        debugHelper.dump();
    }

    private Vector2 forwardLooking(Vector2 desiredVelocity, List<Unit> units, long tickCount) {
        DebugHelper debugHelper = new DebugHelper("forwardLooking", this, true);
        debugHelper.appendAngle("input", desiredVelocity.getDirection());
        ClearanceHole clearanceHole = new ClearanceHole(this);
        for (Unit other : units) {
            if (this == other) {
                continue;
            }
            // Check if other is too far away
            double distance = getDistance(other);
            if (distance > LOOK_AHEAD_DISTANCE) {
                // debugHelper.append("too far away.");
                continue;
            }

            // Check other destination
            if (other.hasDestination()) {
                // Similar destination
                if (other.destination.difference(destination).getMagnitude() <= getRadius() + other.getRadius()) {
                    // debugHelper.append("Similar destination.");
                    continue;
                }

                // Other moves to destination in same direction
                Vector2 relativeDestination = destination.difference(position);
                relativeDestination.normalize();
                Vector2 relativeDestinationOther = other.destination.difference(other.position);
                relativeDestinationOther.normalize();
                double deltaAngle = Math.acos(relativeDestination.dot(relativeDestinationOther));
                if (deltaAngle < Math.PI / 2.0) {
                    // debugHelper.append("move to same direction");
                    continue;
                }
            }

//                // Check if other is in front
//                Vector2 relativePosition = other.getCenter().difference(position);
//                relativePosition.normalize();
//                if (Math.acos(normalizedVelocity.dot(relativePosition)) > Math.PI / 4.0) {
//                    debugHelper.append("not in front.");
//                    continue;
//                }

            //Check if destination is nearer than other
            if (position.distance(destination) < position.distance(other.position)) {
                continue;
            }

            if (!other.hasDestination() && other.lastDestination != null && other.lastDestination.difference(destination).getMagnitude() <= getRadius() + other.getRadius()) {
                continue;
            }

            // Other is dangerous. calculate push away force with velocity - obstacle
            debugHelper.append("dangerous", other);
            clearanceHole.addOther(other);
        }
        double direction = clearanceHole.getFreeAngle(desiredVelocity.getDirection());
        debugHelper.appendAngle("direction", direction);
        Vector2 fixedDesiredVelocity = new Vector2(0, 1);
        fixedDesiredVelocity.setMagnitude(desiredVelocity.getMagnitude());
        fixedDesiredVelocity.setDirection(direction);
        debugHelper.dump();
        return fixedDesiredVelocity;
    }

    public boolean isIndirectOnDestination(Unit other, List<Unit> units, int depth) {
        return isIndirectOnDestination(other, units, depth, true);
    }

    public boolean isIndirectOnDestination(Unit other, List<Unit> units, int depth, boolean isRoot) {
        if (depth < 0) {
            return false;
        }
        if (other.getCenter().distance(destination) <= other.getRadius() + UNIT_DISTANCE_DESTINATION_CHECK) {
            return true;
        }
        if (depth == 0) {
            return false;
        }
        int count = 0;
        boolean found = false;
        for (Unit unit : units) {
            if (this == unit) {
                continue;
            }
            if (other.getCenter().distance(unit.getCenter()) > other.getRadius() + unit.getRadius() + UNIT_DISTANCE_DESTINATION_CHECK) {
                continue;
            }
            count++;
            if (isIndirectOnDestination(unit, units, depth - 1, false)) {
                if (isRoot) {
                    found = true;
                } else {
                    return true;
                }
            }
        }
        return found && count < 2;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public boolean isCanMove() {
        return canMove;
    }

    public boolean checkDestinationReached(Collection<Unit> units) {
        DebugHelper debugHelper = new DebugHelper("dest", this, false);
        debugHelper.append("distance: " + position.distance(destination));
        // 1) Position reached directly
        if (position.distance(destination) < radius + 1) {
            debugHelper.append("true");
            debugHelper.dump();
            return true;
        }
        // 2) None moving neighbor reached destination
        if (isDirectNeighborInDestination(units, destination)) {
            debugHelper.append("direct neighbor");
            debugHelper.dump();
            return true;
        }
        // 3) Indirect contact via at least 2 other units to a unit which stand on the destination
        Collection<Unit> expandedNodes = new ArrayList<>();
        if (isIndirectNeighborInDestination(units, expandedNodes, destination)) {
            debugHelper.append("indirect neighbor");
            debugHelper.dump();
            return true;
        }
        debugHelper.append("false");
        debugHelper.dump();
        return false;
    }

    private Collection<Unit> getNeighbors(Collection<Unit> units) {
        Collection<Unit> neighbors = new ArrayList<>();
        for (Unit neighbor : units) {
            if (neighbor.equals(this)) {
                continue;
            }
            if (getDistance(neighbor) > 1.0) {
                continue;
            }
            neighbors.add(neighbor);
        }
        return neighbors;
    }

    private boolean isDirectNeighborInDestination(Collection<Unit> units, Vector2 destination) {
        for (Unit neighbor : getNeighbors(units)) {
            if (!neighbor.hasDestination() && neighbor.getCenter().distance(destination) < neighbor.getRadius() + 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isIndirectNeighborInDestination(Collection<Unit> units, Collection<Unit> expandedUnits, Vector2 destination) {
        Collection<Unit> neighbors = getNeighbors(units);
        for (Unit neighbor : neighbors) {
            if (neighbor.isDirectNeighborInDestination(units, destination)) {
                return true;
            }
        }
        expandedUnits.add(this);
        int count = 0;
        for (Unit neighbor : neighbors) {
            if (expandedUnits.contains(neighbor)) {
                continue;
            }
            if (neighbor.isIndirectNeighborInDestination(units, expandedUnits, destination)) {
                count++;
                if (count >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "id=" + id +
                ", CFw=" + position +
                ", velocity=" + velocity +
                ", dest=" + destination +
                '}';
    }
}
