package com.btxtech.server.rest.editor;

import java.time.Instant;

/**
 * Lightweight scene record for the studio's scene-list view. Excludes
 * jsonContent to keep the list payload small — full data is fetched on
 * demand via {@code GET /studio-scene/{id}}.
 */
public class StudioSceneSummary {
    private int id;
    private String name;
    private Instant lastModified;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getLastModified() { return lastModified; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }
}
