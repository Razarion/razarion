package com.btxtech.shared.gameengine.datatypes.itemtype;

import org.teavm.flavour.json.JsonPersistable;

@JsonPersistable
public class AudioItemConfig {
    private Integer audioId;
    private int pitchCentsMin = -200;
    private int pitchCentsMax = 200;
    private double volumeMin = 0.8;
    private double volumeMax = 1.0;

    public Integer getAudioId() {
        return audioId;
    }

    public void setAudioId(Integer audioId) {
        this.audioId = audioId;
    }

    public int getPitchCentsMin() {
        return pitchCentsMin;
    }

    public void setPitchCentsMin(int pitchCentsMin) {
        this.pitchCentsMin = pitchCentsMin;
    }

    public int getPitchCentsMax() {
        return pitchCentsMax;
    }

    public void setPitchCentsMax(int pitchCentsMax) {
        this.pitchCentsMax = pitchCentsMax;
    }

    public double getVolumeMin() {
        return volumeMin;
    }

    public void setVolumeMin(double volumeMin) {
        this.volumeMin = volumeMin;
    }

    public double getVolumeMax() {
        return volumeMax;
    }

    public void setVolumeMax(double volumeMax) {
        this.volumeMax = volumeMax;
    }

    public AudioItemConfig audioId(Integer audioId) {
        setAudioId(audioId);
        return this;
    }

    public AudioItemConfig pitchCentsMin(int pitchCentsMin) {
        setPitchCentsMin(pitchCentsMin);
        return this;
    }

    public AudioItemConfig pitchCentsMax(int pitchCentsMax) {
        setPitchCentsMax(pitchCentsMax);
        return this;
    }

    public AudioItemConfig volumeMin(double volumeMin) {
        setVolumeMin(volumeMin);
        return this;
    }

    public AudioItemConfig volumeMax(double volumeMax) {
        setVolumeMax(volumeMax);
        return this;
    }
}
