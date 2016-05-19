package com.btxtech.shared.gameengine.pathing;

import com.btxtech.game.jsre.client.common.DecimalPosition;

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
    private DecimalPosition position;
    private DecimalPosition velocity;
    private DecimalPosition destination;
    private DecimalPosition lastDestination;
    private long lastTickPositionImproved;
    private double nearestDistance;
    private double angle;

    public Unit(int id, boolean canMove, double radius, DecimalPosition position, DecimalPosition destination, DecimalPosition lastDestination, long tickCount) {
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
            nearestDistance = position.getDistance(destination);
            lastTickPositionImproved = tickCount;
        }
    }

    public int getId() {
        return id;
    }

    public double getRadius() {
        return radius;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public void addToCenter(DecimalPosition delta, long tickCount) {
        position = position.add(delta);
        updateNearestDistance(tickCount);
    }

    private void updateNearestDistance(long tickCount) {
        if (destination != null) {
            double newDistance = position.getDistance(destination);
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
        DecimalPosition norm = position.sub(other.getPosition()).normalize();
        return new Contact(this, other, norm);
    }

    public double getDistance(Unit other) {
        return position.getDistance(other.getPosition()) - radius - other.getRadius();
    }

    public void implementPosition(long tickCount) {
        DebugHelper debugHelper = new DebugHelper("imp1", this, false);
        position = getDesiredPosition();
        updateNearestDistance(tickCount);
        debugHelper.append("np", position);
        //debugHelper.append("time", system.currentTimeMillis());
        debugHelper.dump();
    }

    public DecimalPosition getDesiredPosition() {
        DecimalPosition desiredPosition = new DecimalPosition(position);
        if (velocity != null) {
            desiredPosition =
                    desiredPosition.add(velocity.multiply(Pathing.FACTOR));
        }
        return desiredPosition;
    }

    public boolean hasDestination() {
        return destination != null;
    }

    public DecimalPosition getDestination() {
        return destination;
    }

    public DecimalPosition getLastDestination() {
        return lastDestination;
    }

    public boolean isMoving() {
        return velocity != null && !velocity.equalsDeltaZero();
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
            DecimalPosition desiredVelocity = destination.sub(position).normalize(SPEED);
            debugHelper.append("desired vl", desiredVelocity);
            if (velocity == null) {
                velocity = DecimalPosition.createVector(angle, 0.001);
            }
            debugHelper.appendAngle("angle from v", velocity.getAngle());
            desiredVelocity = forwardLooking(desiredVelocity, units, tickCount).normalize(SPEED);
            double desiredAngle = desiredVelocity.getAngle();
            debugHelper.append("desired v", desiredVelocity);
            debugHelper.appendAngle("desired a", desiredVelocity.getAngle());
            // double deltaAngle = velocity.getAngleBetween(desiredVelocity);
            double deltaAngle = Pathing.correctAngle(desiredVelocity.getAngle() - angle);
            debugHelper.appendAngle("deltaAngle", deltaAngle);
            // Fix angle
            double angleSpeedFactor = 1.0;
            if (Math.abs(deltaAngle) > ANGULAR_VELOCITY * Pathing.FACTOR) {
                debugHelper.append("angle too big");
                double possibleAngle = Pathing.correctAngle(angle + Math.signum(deltaAngle) * ANGULAR_VELOCITY * Pathing.FACTOR);
                debugHelper.appendAngle("possibleAngle", possibleAngle);
                angle = possibleAngle;
                DecimalPosition desiredVelocityNorm = desiredVelocity.normalize();
                DecimalPosition fixedAngleVelocityNorm = DecimalPosition.createVector(possibleAngle, 1.0);
                angleSpeedFactor = Math.max(0.0, Math.min(1.0, fixedAngleVelocityNorm.dotProduct(desiredVelocityNorm)));
            } else {
                angle = desiredAngle;
            }
            debugHelper.appendAngle("angle", angle);
            // Fix velocity
            double originalSpeed = velocity.magnitude(); // TODO That is wrong
            debugHelper.append("originalSpeed", originalSpeed);
            double possibleSpeed = Math.max(MIN_SPEED, angleSpeedFactor * SPEED);
            debugHelper.append("possibleSpeed", possibleSpeed);
            double speed;
            if (Math.abs(originalSpeed - possibleSpeed) > ACCELERATION * Pathing.FACTOR) {
                if (originalSpeed < possibleSpeed) {
                    debugHelper.append("accelerate");
                    speed = originalSpeed + ACCELERATION * Pathing.FACTOR;
                } else {
                    debugHelper.append("slow down");
                    speed = originalSpeed - ACCELERATION * Pathing.FACTOR;
                }
            } else {
                debugHelper.append("speed ok");
                speed = possibleSpeed;
            }
            // Check if destination too near to turn
            deltaAngle = Pathing.correctAngle(desiredVelocity.getAngle() - angle);
            double turnSteps = Math.abs(deltaAngle) / (ANGULAR_VELOCITY * Pathing.FACTOR);
            double distance = turnSteps * speed * Pathing.FACTOR;
            if (distance > position.getDistance(destination)) {
                speed = originalSpeed - ACCELERATION * Pathing.FACTOR;
                debugHelper.append("turn angle too big");
            }
            speed = Math.min(SPEED, speed);
            speed = Math.max(0.0, speed);
            velocity = DecimalPosition.createVector(angle, speed);

            debugHelper.append("magnitude", velocity.magnitude());
        } else {
            if (velocity == null) {
                debugHelper.dump();
                return;
            }
            double magnitude = velocity.magnitude();
            double acceleration = ACCELERATION * Pathing.FACTOR;
            if (acceleration >= magnitude) {
                velocity = null;
            } else {
                velocity = velocity.normalize(magnitude - acceleration);
                if (velocity.equalsDeltaZero()) {
                    velocity = null;
                }
            }
        }
        debugHelper.dump();
    }

    private DecimalPosition forwardLooking(DecimalPosition desiredVelocity, List<Unit> units, long tickCount) {
        DebugHelper debugHelper = new DebugHelper("forwardLooking", this, false);
        debugHelper.appendAngle("input", desiredVelocity.getAngle());
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
                if (other.destination.sub(destination).magnitude() <= getRadius() + other.getRadius()) {
                    // debugHelper.append("Similar destination.");
                    continue;
                }

                // Other moves to destination in same direction
                DecimalPosition relativeDestination = destination.sub(position).normalize();
                ;
                DecimalPosition relativeDestinationOther = other.destination.sub(other.position).normalize();
                double deltaAngle = Math.acos(relativeDestination.dotProduct(relativeDestinationOther));
                if (deltaAngle < Math.PI / 2.0) {
                    // debugHelper.append("move to same direction");
                    continue;
                }
            }

//                // Check if other is in front
//                DecimalPosition relativePosition = other.getPosition().sub(position);
//                !relativePosition.normalize();
//                if (Math.acos(normalizedVelocity.dot(relativePosition)) > Math.PI / 4.0) {
//                    debugHelper.append("not in front.");
//                    continue;
//                }

            //Check if destination is nearer than other
            if (position.getDistance(destination) < position.getDistance(other.position)) {
                continue;
            }

            if (!other.hasDestination() && other.lastDestination != null && other.lastDestination.sub(destination).magnitude() <= getRadius() + other.getRadius()) {
                continue;
            }

            // Other is dangerous. calculate push away force with velocity - obstacle
            debugHelper.append("dangerous", other);
            clearanceHole.addOther(other);
        }
        double direction = clearanceHole.getFreeAngle(desiredVelocity.getAngle());
        debugHelper.appendAngle("direction", direction);
        debugHelper.dump();
        return DecimalPosition.createVector(direction, desiredVelocity.magnitude());
    }

    public boolean isIndirectOnDestination(Unit other, List<Unit> units, int depth) {
        return isIndirectOnDestination(other, units, depth, true);
    }

    public boolean isIndirectOnDestination(Unit other, List<Unit> units, int depth, boolean isRoot) {
        if (depth < 0) {
            return false;
        }
        if (other.getPosition().getDistance(destination) <= other.getRadius() + UNIT_DISTANCE_DESTINATION_CHECK) {
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
            if (other.getPosition().getDistance(unit.getPosition()) > other.getRadius() + unit.getRadius() + UNIT_DISTANCE_DESTINATION_CHECK) {
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

    public DecimalPosition getVelocity() {
        return velocity;
    }

    public void setVelocity(DecimalPosition velocity) {
        this.velocity = velocity;
    }

    public boolean isCanMove() {
        return canMove;
    }

    public boolean checkDestinationReached(Collection<Unit> units) {
        DebugHelper debugHelper = new DebugHelper("dest", this, false);
        debugHelper.append("distance: " + position.getDistance(destination));
        // 1) Position reached directly
        if (position.getDistance(destination) < radius + 1) {
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

    private boolean isDirectNeighborInDestination(Collection<Unit> units, DecimalPosition destination) {
        for (Unit neighbor : getNeighbors(units)) {
            if (!neighbor.hasDestination() && neighbor.getPosition().getDistance(destination) < neighbor.getRadius() + 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isIndirectNeighborInDestination(Collection<Unit> units, Collection<Unit> expandedUnits, DecimalPosition destination) {
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

    public Unit getCopy() {
        Unit unit = new Unit(id, canMove, radius, position, destination, lastDestination, lastTickPositionImproved);
        unit.velocity = velocity;
        unit.nearestDistance = nearestDistance;
        unit.angle = angle;
        return unit;
    }

    public boolean isInside(DecimalPosition position) {
        return this.position.getDistance(position) <= radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Unit unit = (Unit) o;
        return id == unit.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Unit{" +
                "id=" + id +
                ", position=" + position +
                ", velocity=" + velocity +
                ", destination=" + destination +
                '}';
    }
}
