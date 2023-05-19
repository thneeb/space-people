package de.neebs.spacepeoples.integration.database;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "PLANET_BUILDING")
@IdClass(BuildingId.class)
public class Building {
    @Id
    @Column(name = "PLANET_ID")
    private String planetId;
    @Id
    @Column(name = "BUILDING_TYPE")
    private String buildingType;

    @Column(name = "BUILDING_STATUS")
    private String status;

    @Column(name = "LEVEL")
    private int level;
}
