package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.gameengine.datatypes.itemtype.HouseType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "BASE_ITEM_HOUSE_TYPE")
public class HouseTypeEntity extends BaseEntity {

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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
