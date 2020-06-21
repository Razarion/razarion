package com.btxtech.shared.utils;

/**
 * Created by Beat
 * on 12.01.2019.
 */
public interface SignalGenerator {

    static double triangle(long millis, int durationMs, int offsetMs) {
        long totMillis = millis + offsetMs;
        int mod = (int) (totMillis % durationMs);
        if (mod < durationMs / 2) {
            // Raising
            return 2.0 * mod / durationMs;
        } else {
            // Falling
            return 2.0 - 2.0 * mod / durationMs;
        }
    }

    static double sawtooth(long millis, int durationMs, int offsetMs) {
        if(durationMs == 0.0) {
            return 0;
        }
        long totMillis = millis + offsetMs;
        return (double) (totMillis % durationMs) / (double)durationMs; // Saegezahn
    }

    static double sinus(long millis, int durationMs, int offsetMs) {
        if(durationMs == 0.0) {
            return 0;
        }
        return (1.0 + Math.sin(((millis % durationMs) / (double) durationMs + ((double) offsetMs / (double) durationMs)) * MathHelper.ONE_RADIANT)) / 2.0;
    }


}
