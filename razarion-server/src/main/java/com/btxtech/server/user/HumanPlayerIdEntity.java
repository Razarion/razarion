package com.btxtech.server.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 23.04.2017.
 */
@Entity
@Table(name = "HUMAN_PLAYER_ENTITY")
public class HumanPlayerIdEntity {
    @Id
    @GeneratedValue
    private Integer id;
//    @OneToOne
//    private UserEntity userEntity;

    public Integer getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HumanPlayerIdEntity that = (HumanPlayerIdEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
