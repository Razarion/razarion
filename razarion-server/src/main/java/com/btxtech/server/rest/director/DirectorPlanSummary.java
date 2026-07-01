package com.btxtech.server.rest.director;

import java.time.Instant;

/**
 * Lightweight plan record for the studio's plan-list view (no jsonContent).
 * Mirrors {@code StudioSceneSummary}.
 */
public class DirectorPlanSummary {
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
