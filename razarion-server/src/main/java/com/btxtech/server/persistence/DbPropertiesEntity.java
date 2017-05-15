package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.Color;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 15.05.2017.
 */
@Entity
@Table(name = "PROPERTY")
public class DbPropertiesEntity {
    @Id
    private String propertyKey;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private AudioLibraryEntity audio;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity image;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity shape3DId;
    private Integer intValue;
    private Double doubleValue;
    @AttributeOverrides({
            @AttributeOverride(name = "r", column = @Column(name = "colorRValue")),
            @AttributeOverride(name = "g", column = @Column(name = "colorGValue")),
            @AttributeOverride(name = "b", column = @Column(name = "colorBValue")),
            @AttributeOverride(name = "a", column = @Column(name = "colorAValue")),
    })
    private Color color;

    /**
     * Used by JPA
     */
    public DbPropertiesEntity() {
    }

    public DbPropertiesEntity(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public AudioLibraryEntity getAudio() {
        return audio;
    }

    public void setAudio(AudioLibraryEntity audio) {
        this.audio = audio;
    }

    public ImageLibraryEntity getImage() {
        return image;
    }

    public void setImage(ImageLibraryEntity image) {
        this.image = image;
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

    public ColladaEntity getShape3DId() {
        return shape3DId;
    }

    public void setShape3DId(ColladaEntity shape3DId) {
        this.shape3DId = shape3DId;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DbPropertiesEntity that = (DbPropertiesEntity) o;

        return propertyKey.equals(that.propertyKey);
    }

    @Override
    public int hashCode() {
        return propertyKey.hashCode();
    }
}
