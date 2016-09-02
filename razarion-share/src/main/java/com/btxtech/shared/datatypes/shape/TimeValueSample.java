package com.btxtech.shared.datatypes.shape;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class TimeValueSample {
    private long timeStamp;
    private double value;

    public long getTimeStamp() {
        return timeStamp;
    }

    public TimeValueSample setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    public double getValue() {
        return value;
    }

    public TimeValueSample setValue(double value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TimeValueSample that = (TimeValueSample) o;

        return timeStamp == that.timeStamp && Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (timeStamp ^ (timeStamp >>> 32));
        temp = Double.doubleToLongBits(value);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TimeValueSample{" +
                "timeStamp=" + timeStamp +
                ", value=" + value +
                '}';
    }
}
