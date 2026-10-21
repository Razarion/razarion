package com.btxtech.server.rest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A fixed call sign for a player without a name, e.g. "Falcon-42", the same for the same userId -
 * which an anonymous player keeps for 14 days (raz_anon). No unit names (Viper, Badger, Hydra ...):
 * "Viper-17 built a Viper" reads like a bug.
 */
public final class CallSign {
    private static final String[] WORDS = {
            "Falcon", "Hawk", "Wolf", "Raven", "Cobra", "Lynx", "Bison", "Orca", "Puma", "Jaguar",
            "Mantis", "Kestrel", "Osprey", "Fox", "Bear", "Shark", "Eagle", "Tiger", "Coyote", "Hornet",
            "Stingray", "Panther", "Condor", "Heron", "Otter", "Moose", "Rhino", "Gecko", "Comet", "Nova",
            "Orion", "Vega", "Sirius", "Titan", "Echo", "Delta", "Sierra", "Tango", "Zulu", "Nomad",
            "Ranger", "Spark", "Storm", "Frost", "Ember", "Granite", "Cobalt", "Onyx", "Quartz", "Jade"
    };

    private CallSign() {
    }

    public static String of(String userId) {
        if (userId == null) {
            return "A commander";
        }
        int hash = stableHash(userId);
        String word = WORDS[Math.floorMod(hash, WORDS.length)];
        int number = 10 + Math.floorMod(hash / WORDS.length, 90);
        return word + "-" + number;
    }

    /** Not String.hashCode: that is stable too, but a digest spreads similar ids better. */
    private static int stableHash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return ((digest[0] & 0xff) << 24) | ((digest[1] & 0xff) << 16) | ((digest[2] & 0xff) << 8) | (digest[3] & 0xff);
        } catch (NoSuchAlgorithmException e) {
            return text.hashCode();
        }
    }
}
