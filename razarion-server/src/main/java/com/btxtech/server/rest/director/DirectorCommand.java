package com.btxtech.server.rest.director;

/**
 * A transport command relayed studio → director-mode client (the studio has no
 * game engine, so it drives the rendering client through this channel). The
 * server just holds the latest command; the client polls {@code GET /command}
 * and acts on any {@code seq} it hasn't seen yet.
 *
 * {@code type} is one of: LOAD_PLAN, PLAY, PAUSE, STOP, SEEK, RECORD_START,
 * RECORD_STOP. {@code seq} is assigned by the server on POST.
 */
public class DirectorCommand {
    private long seq;
    private String type;
    /** LOAD_PLAN: which plan to fetch. */
    private Integer planId;
    /** SEEK / PLAY-from: timeline position in ms. */
    private Integer timeMs;
    /** RECORD_START: download file name. */
    private String fileName;

    public long getSeq() { return seq; }
    public void setSeq(long seq) { this.seq = seq; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }
    public Integer getTimeMs() { return timeMs; }
    public void setTimeMs(Integer timeMs) { this.timeMs = timeMs; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
