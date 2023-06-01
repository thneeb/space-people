package de.neebs.spacepeoples.integration.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "PLANET_CAPACITY_SUPPLY")
@IdClass(PlanetCapacityId.class)
public class PlanetCapacitySupply {
    @Id
    @Column(name = "PLANET_ID")
    private String planetId;

    @Id
    @Column(name = "CAPACITY_TYPE")
    private String capacityType;

    @Column(name = "CAPACITY_SUPPLY")
    private long capacitySupply;
}
