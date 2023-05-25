package de.neebs.spacepeoples.integration.database;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "PLANET_RECYCLE_RESOURCE")
@IdClass(PlanetResourceId.class)
public class PlanetRecycleResource {
    @Id
    @Column(name = "PLANET_ID")
    private String planetId;

    @Id
    @Column(name = "RESOURCE_TYPE")
    private String resourceType;

    @Column(name = "UNITS")
    private int units;
}
