package com.btxtech.server.rest.director;

import java.util.List;

/** Outcome of a {@link StageAttackRequest}: what was actually spawned + ordered. */
public class StageAttackResult {
    private int spawned;
    private String attackerType;
    private int targetBaseId;
    private int targetItemId;
    /** Per-unit spawn failures (e.g. item limit / house space / terrain), if any. */
    private List<String> errors;

    public StageAttackResult() {
    }

    public StageAttackResult(int spawned, String attackerType, int targetBaseId, int targetItemId, List<String> errors) {
        this.spawned = spawned;
        this.attackerType = attackerType;
        this.targetBaseId = targetBaseId;
        this.targetItemId = targetItemId;
        this.errors = errors;
    }

    public int getSpawned() { return spawned; }
    public void setSpawned(int spawned) { this.spawned = spawned; }
    public String getAttackerType() { return attackerType; }
    public void setAttackerType(String attackerType) { this.attackerType = attackerType; }
    public int getTargetBaseId() { return targetBaseId; }
    public void setTargetBaseId(int targetBaseId) { this.targetBaseId = targetBaseId; }
    public int getTargetItemId() { return targetItemId; }
    public void setTargetItemId(int targetItemId) { this.targetItemId = targetItemId; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
