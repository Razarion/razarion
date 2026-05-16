package com.btxtech.server.model.studio;

import com.btxtech.server.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A studio-composed scene snapshot — items, particles, terrain, camera —
 * stored as opaque JSON. The studio defines and consumes the schema; the
 * server just persists the blob plus a human-readable name and timestamp.
 *
 * Lives in its own package because this isn't game-engine data — it's an
 * editor artefact used for landing-page and social-media renders.
 */
@Entity
@Table(name = "STUDIO_SCENE")
public class StudioSceneEntity extends BaseEntity {
    /** Human-readable scene name, shown in the studio's scene list. */
    @Column(name = "scene_name", nullable = false, length = 255)
    private String sceneName;

    /** Opaque JSON blob — schema lives in the studio frontend. */
    @Lob
    @Column(name = "json_content", columnDefinition = "LONGTEXT")
    private String jsonContent;

    @Column(name = "last_modified")
    private Instant lastModified;

    public String getSceneName() { return sceneName; }
    public void setSceneName(String sceneName) { this.sceneName = sceneName; }
    public String getJsonContent() { return jsonContent; }
    public void setJsonContent(String jsonContent) { this.jsonContent = jsonContent; }
    public Instant getLastModified() { return lastModified; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }
}
