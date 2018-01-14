package com.btxtech.client;

import com.btxtech.shared.gameengine.GameEngineControlPackage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 14.01.2018.
 */
public class QueueStatistics {
    private Logger logger = Logger.getLogger(QueueStatistics.class.getName());
    private static final int DURATION = 30000;
    private long lastDump = -1;
    private Map<GameEngineControlPackage.Command, Integer> sendTotal = new HashMap<>();
    private Map<GameEngineControlPackage.Command, Integer> sendPeriod = new HashMap<>();
    private Map<GameEngineControlPackage.Command, Integer> receivedTotal = new HashMap<>();
    private Map<GameEngineControlPackage.Command, Integer> receivedPeriod = new HashMap<>();

    public void send(GameEngineControlPackage.Command command) {
        increase(command, sendTotal);
        increase(command, sendPeriod);
        handlePeriod();
    }

    public void received(GameEngineControlPackage.Command command) {
        increase(command, receivedTotal);
        increase(command, receivedPeriod);
        handlePeriod();
    }

    private void increase(GameEngineControlPackage.Command command, Map<GameEngineControlPackage.Command, Integer> map) {
        Integer count = map.get(command);
        if (count == null) {
            count = 0;
        }
        count++;
        map.put(command, count);
    }

    private void handlePeriod() {
        if (lastDump < 0) {
            lastDump = System.currentTimeMillis();
            return;
        }
        if (lastDump + DURATION > System.currentTimeMillis()) {
            return;
        }
        long delta = System.currentTimeMillis() - lastDump;
        dump((double)delta / 1000.0);
        sendPeriod.clear();
        receivedPeriod.clear();
        lastDump = System.currentTimeMillis();
    }

    private void dump(double periodInSeconds) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nSend total\n");
        fill(sendTotal, sendPeriod, periodInSeconds, stringBuilder);
        stringBuilder.append("Received total\n");
        fill(receivedTotal, receivedPeriod, periodInSeconds, stringBuilder);
        logger.severe(stringBuilder.toString());
    }

    public void fill(Map<GameEngineControlPackage.Command, Integer> total, Map<GameEngineControlPackage.Command, Integer> period, double periodInSeconds, StringBuilder stringBuilder) {
        Set<GameEngineControlPackage.Command> allCommands = new HashSet<>(total.keySet());
        allCommands.addAll(period.keySet());
        allCommands.stream().sorted(Comparator.comparing(Enum::toString)).forEach(command -> {
            stringBuilder.append("\t");
            stringBuilder.append(command);
            stringBuilder.append("\t\t\t");
            if (total.containsKey(command)) {
                stringBuilder.append(total.get(command));
            } else {
                stringBuilder.append("\t");
            }
            stringBuilder.append("    ");
            if (period.containsKey(command)) {
                stringBuilder.append((double) period.get(command) / periodInSeconds);
                stringBuilder.append("/s");
            } else {
                stringBuilder.append("\t");
            }
            stringBuilder.append("\n");
        });
    }

}
