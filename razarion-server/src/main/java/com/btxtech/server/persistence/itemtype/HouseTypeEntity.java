package com.btxtech.server.persistence.itemtype;

import com.btxtech.shared.gameengine.datatypes.itemtype.HouseType;

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
@Table(name = "BASE_ITEM_HOUSE_TYPE")
public class HouseTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int space;

    public HouseType toHouseType() {
        return new HouseType().space(space);
    }

    public void fromHouseType(HouseType houseType) {
        space = houseType.getSpace();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HouseTypeEntity that = (HouseTypeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
