package com.btxtech.shared.gameengine.datatypes.packets;

import java.util.Map;

/**
 * Created by Beat
 * on 09.08.2017.
 */
public class QuestProgressInfo {
    private Integer count;
    private Map<Integer, Integer> typeCount;
    private Integer time;
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

    public QuestProgressInfo setTypeCount(Map<Integer, Integer> typeCount) {
        this.typeCount = typeCount;
        return this;
    }

    public Integer getTime() {
        return time;
    }

    public QuestProgressInfo setTime(Integer time) {
        this.time = time;
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
