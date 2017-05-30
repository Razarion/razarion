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

    public GameUiControlInput setPlaybackGameSessionUuid(String playbackGameSessionUuid) {
        this.playbackGameSessionUuid = playbackGameSessionUuid;
        return this;
    }

    public String getPlaybackSessionUuid() {
        return playbackSessionUuid;
    }

    public GameUiControlInput setPlaybackSessionUuid(String playbackSessionUuid) {
        this.playbackSessionUuid = playbackSessionUuid;
        return this;
    }

    public boolean checkPlayback() {
        return playbackGameSessionUuid != null && playbackSessionUuid != null;
    }
}
