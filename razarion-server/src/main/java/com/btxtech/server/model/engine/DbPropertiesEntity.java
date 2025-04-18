package com.btxtech.server.model.engine;

import com.btxtech.server.model.ui.BabylonMaterialEntity;
import com.btxtech.server.model.ui.ImageLibraryEntity;
import com.btxtech.shared.datatypes.Color;
import jakarta.persistence.*;

@Entity
@Table(name = "PROPERTY")
public class DbPropertiesEntity {
    @Id
    @Column(length = 190)
    // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private String propertyKey;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private AudioLibraryEntity audio;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity image;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BabylonMaterialEntity babylonMaterialEntity;
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

    public BabylonMaterialEntity getBabylonMaterialEntity() {
        return babylonMaterialEntity;
    }

    public void setBabylonMaterialEntity(BabylonMaterialEntity babylonMaterial) {
        this.babylonMaterialEntity = babylonMaterial;
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
