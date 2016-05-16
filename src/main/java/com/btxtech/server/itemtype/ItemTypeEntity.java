package com.btxtech.server.itemtype;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Created by Beat
 * 15.05.2016.
 */
@Entity
@Table(name = "ITEM_TYPE")
public class ItemTypeEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Lob
    @Basic(optional = false)
    private String colladaString;

    public Long getId() {
        return id;
    }

    public String getColladaString() {
        return colladaString;
    }

    public void setColladaString(String colladaString) {
        this.colladaString = colladaString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemTypeEntity that = (ItemTypeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
