package com.btxtech.shared.dto;

/**
 * Created by Beat
 * on 30.05.2017.
 */
public class GameUiControlInput {
    private String playbackGameSessionUuid;
    private String playbackSessionUuid;

    public String getPlaybackGameSessionUuid() {
        return playbackGameSessionUuid;
    }

    public void setPlaybackGameSessionUuid(String playbackGameSessionUuid) {
        this.playbackGameSessionUuid = playbackGameSessionUuid;
    }

    public String getPlaybackSessionUuid() {
        return playbackSessionUuid;
    }

    public void setPlaybackSessionUuid(String playbackSessionUuid) {
        this.playbackSessionUuid = playbackSessionUuid;
    }

    public GameUiControlInput playbackGameSessionUuid(String playbackGameSessionUuid) {
        setPlaybackGameSessionUuid(playbackGameSessionUuid);
        return this;
    }

    public GameUiControlInput playbackSessionUuid(String playbackSessionUuid) {
        setPlaybackSessionUuid(playbackSessionUuid);
        return this;
    }

    public boolean checkPlayback() {
        return playbackGameSessionUuid != null && playbackSessionUuid != null;
    }
}
