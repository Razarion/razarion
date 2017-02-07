package com.btxtech.uiservice.particle;

/**
 * Created by Beat
 * 07.02.2017.
 */
public class ParticleConfig {
    // private Logger logger = Logger.getLogger(ParticleConfig.class.getName());
    private int particleShapeConfigId;
    private Double particleGrow;
    private int timeToLive;
    private Integer timeToLiveRandomPart;
    private Double speedX;
    private Double speedXRandomPart;
    private Double speedY;
    private Double speedYRandomPart;
    private Double speedZ;
    private Double speedZRandomPart;

    public int getParticleShapeConfigId() {
        return particleShapeConfigId;
    }

    public ParticleConfig setParticleShapeConfigId(int particleShapeConfigId) {
        this.particleShapeConfigId = particleShapeConfigId;
        return this;
    }

    public Double getParticleGrow() {
        return particleGrow;
    }

    public ParticleConfig setParticleGrow(Double particleGrow) {
        this.particleGrow = particleGrow;
        return this;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public ParticleConfig setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }

    public Integer getTimeToLiveRandomPart() {
        return timeToLiveRandomPart;
    }

    public ParticleConfig setTimeToLiveRandomPart(Integer timeToLiveRandomPart) {
        this.timeToLiveRandomPart = timeToLiveRandomPart;
        return this;
    }

    public Double getSpeedX() {
        return speedX;
    }

    public ParticleConfig setSpeedX(Double speedX) {
        this.speedX = speedX;
        return this;
    }

    public Double getSpeedXRandomPart() {
        return speedXRandomPart;
    }

    public ParticleConfig setSpeedXRandomPart(Double speedXRandomPart) {
        this.speedXRandomPart = speedXRandomPart;
        return this;
    }

    public Double getSpeedY() {
        return speedY;
    }

    public ParticleConfig setSpeedY(Double speedY) {
        this.speedY = speedY;
        return this;
    }

    public Double getSpeedYRandomPart() {
        return speedYRandomPart;
    }

    public ParticleConfig setSpeedYRandomPart(Double speedYRandomPart) {
        this.speedYRandomPart = speedYRandomPart;
        return this;
    }

    public Double getSpeedZ() {
        return speedZ;
    }

    public ParticleConfig setSpeedZ(Double speedZ) {
        this.speedZ = speedZ;
        return this;
    }

    public Double getSpeedZRandomPart() {
        return speedZRandomPart;
    }

    public ParticleConfig setSpeedZRandomPart(Double speedZRandomPart) {
        this.speedZRandomPart = speedZRandomPart;
        return this;
    }

    public boolean validSpeed() {
        return speedX != null || speedY != null || speedZ != null;
    }
}
