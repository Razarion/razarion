package com.btxtech.server.rest.director;

/**
 * Whether a rendering client is listening, and which command it has had the chance to see.
 * <p>
 * {@code clientLastSeenMillisAgo} is null when no client has ever polled since the server started.
 * A few hundred milliseconds means one is polling right now; anything above a couple of seconds
 * means the tab is gone, asleep, or answering 403 to every poll.
 */
public class DirectorStatus {
    private Long clientLastSeenMillisAgo;
    private Long lastCommandSeq;

    public DirectorStatus() {
    }

    public DirectorStatus(Long clientLastSeenMillisAgo, Long lastCommandSeq) {
        this.clientLastSeenMillisAgo = clientLastSeenMillisAgo;
        this.lastCommandSeq = lastCommandSeq;
    }

    public Long getClientLastSeenMillisAgo() {
        return clientLastSeenMillisAgo;
    }

    public void setClientLastSeenMillisAgo(Long clientLastSeenMillisAgo) {
        this.clientLastSeenMillisAgo = clientLastSeenMillisAgo;
    }

    public Long getLastCommandSeq() {
        return lastCommandSeq;
    }

    public void setLastCommandSeq(Long lastCommandSeq) {
        this.lastCommandSeq = lastCommandSeq;
    }
}
