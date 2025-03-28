package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_CONSUMER_TYPE")
public class ConsumerTypeEntity extends BaseEntity {

    private int wattage;

    public ConsumerType toConsumerType() {
        return new ConsumerType().setWattage(wattage);
    }

    public void fromConsumerType(ConsumerType consumerType) {
        wattage = consumerType.getWattage();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConsumerTypeEntity that = (ConsumerTypeEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
