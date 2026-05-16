package com.btxtech.server.rest.editor;

import java.time.Instant;

/**
 * Full scene payload: meta + opaque JSON blob. Used by GET /{id}, POST (create)
 * and POST /{id} (update). On create the {@code id} is ignored.
 */
public class StudioSceneDto {
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
