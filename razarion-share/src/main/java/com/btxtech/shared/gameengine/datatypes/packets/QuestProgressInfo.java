package com.btxtech.shared.gameengine.datatypes.packets;

import jsinterop.annotations.JsType;

import java.util.Map;

/**
 * Created by Beat
 * on 09.08.2017.
 */
@JsType
public class QuestProgressInfo {
    private Integer count;
    private Map<Integer, Integer> typeCount;
    private Integer secondsRemaining;
    private String botBasesInformation;

    public Integer getCount() {
        return count;
    }

    public QuestProgressInfo setCount(Integer count) {
        this.count = count;
        return this;
    }

    public Map<Integer, Integer> getTypeCount() {
        return typeCount;
    }

    /**
     *
     * @return Key Item Type, Value count
     */
    public Integer[][] toTypeCountAngular() {
        if (typeCount == null) {
            return null;
        }

        Integer[][] types = new Integer[typeCount.size()][];
        int index = 0;
        for (Map.Entry<Integer, Integer> entry : typeCount.entrySet()) {
            types[index++] = new Integer[]{entry.getKey(), entry.getValue()};
        }
        return types;
    }

    public QuestProgressInfo setTypeCount(Map<Integer, Integer> typeCount) {
        this.typeCount = typeCount;
        return this;
    }

    public Integer getSecondsRemaining() {
        return secondsRemaining;
    }

    public QuestProgressInfo setSecondsRemaining(Integer secondsRemaining) {
        this.secondsRemaining = secondsRemaining;
        return this;
    }

    public String getBotBasesInformation() {
        return botBasesInformation;
    }

    public QuestProgressInfo setBotBasesInformation(String botBasesInformation) {
        this.botBasesInformation = botBasesInformation;
        return this;
    }
}
