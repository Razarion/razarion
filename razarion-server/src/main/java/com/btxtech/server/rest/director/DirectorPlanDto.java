package com.btxtech.server.rest.director;

import java.time.Instant;

/**
 * Full director-plan payload: meta + opaque JSON blob (camera keyframes + cues).
 * Mirrors {@code StudioSceneDto}. On create the {@code id} is ignored.
 */
public class DirectorPlanDto {
    private int id;
    private String name;
    private String jsonContent;
    private Instant lastModified;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getJsonContent() { return jsonContent; }
    public void setJsonContent(String jsonContent) { this.jsonContent = jsonContent; }
    public Instant getLastModified() { return lastModified; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }
}
