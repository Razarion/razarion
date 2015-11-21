package com.btxtech.server.terrain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Beat
 * 21.11.2015.
 */
@Entity
@Table(name = "TERRAIN_PLATEAU")
public class PlateauEntity {
    @Id
    @GeneratedValue
    private Long id;

}
