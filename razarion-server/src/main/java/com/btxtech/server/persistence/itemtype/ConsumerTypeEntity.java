package com.btxtech.server.persistence.itemtype;

import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_CONSUMER_TYPE")
public class ConsumerTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
