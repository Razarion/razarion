package com.btxtech.shared.system.alarm;

public class AlarmRaisedException extends RuntimeException {
    private final Alarm.Type type;
    private final String text;
    private final Integer id;

    public AlarmRaisedException(Alarm.Type type, String text) {
        this(type, text, null);
    }

    public AlarmRaisedException(Alarm.Type type, String text, Integer id) {
        super(type + ": " + text);
        this.type = type;
        this.text = text;
        this.id = id;
    }

    public Alarm.Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "AlarmRaisedException{" +
                "type=" + type +
                ", text='" + text + '\'' +
                ", id=" + id +
                '}';
    }
}
