package com.btxtech.server.model.director;

import com.btxtech.server.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A "director" plan: a camera flight path (+ optional action cues) used to film
 * the live local game world for social-media clips. Stored as an opaque JSON
 * blob exactly like {@link com.btxtech.server.model.studio.StudioSceneEntity} —
 * the schema lives in the frontend (studio authors it, the director-mode client
 * consumes it).
 *
 * This is a dev-only authoring artefact; the REST surface that reads/writes it
 * ({@code DirectorController}) is gated behind {@code razarion.director.enabled}
 * and absent on prod.
 */
@Entity
@Table(name = "DIRECTOR_PLAN")
public class DirectorPlanEntity extends BaseEntity {
    /** Human-readable plan name, shown in the studio's plan list. */
    @Column(name = "plan_name", nullable = false, length = 255)
    private String planName;

    /** Opaque JSON blob — schema lives in the frontend. */
    @Lob
    @Column(name = "json_content", columnDefinition = "LONGTEXT")
    private String jsonContent;

    @Column(name = "last_modified")
    private Instant lastModified;

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getJsonContent() { return jsonContent; }
    public void setJsonContent(String jsonContent) { this.jsonContent = jsonContent; }
    public Instant getLastModified() { return lastModified; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }
}
