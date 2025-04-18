package com.btxtech.server.model.engine;

import com.btxtech.shared.datatypes.DbPropertyType;

public class DbPropertyConfig {
    private String key;
    private String stringValue;
    private Integer intValue;
    private Double doubleValue;
    private DbPropertyType dbPropertyType;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public DbPropertyType getDbPropertyType() {
        return dbPropertyType;
    }

    public void setDbPropertyType(DbPropertyType dbPropertyType) {
        this.dbPropertyType = dbPropertyType;
    }

    public DbPropertyConfig key(String key) {
        setKey(key);
        return this;
    }

    public DbPropertyConfig stringValue(String stringValue) {
        setStringValue(stringValue);
        return this;
    }

    public DbPropertyConfig intValue(Integer intValue) {
        setIntValue(intValue);
        return this;
    }

    public DbPropertyConfig doubleValue(Double doubleValue) {
        setDoubleValue(doubleValue);
        return this;
    }

    public DbPropertyConfig dbPropertyType(DbPropertyType dbPropertyType) {
        setDbPropertyType(dbPropertyType);
        return this;
    }
}
