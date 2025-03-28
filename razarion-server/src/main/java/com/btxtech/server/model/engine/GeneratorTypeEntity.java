package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.GeneratorType;

import jakarta.persistence.*;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_GENERATOR_TYPE")

public class GeneratorTypeEntity extends BaseEntity {
    
    private int wattage;

    public GeneratorType toGeneratorType() {
        return new GeneratorType().setWattage(wattage);
    }

    public void fromGeneratorType(GeneratorType generatorType) {
        wattage = generatorType.getWattage();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GeneratorTypeEntity that = (GeneratorTypeEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
