package de.neebs.spacepeoples.integration.database;

import de.neebs.spacepeoples.entity.ResourceLevel;
import de.neebs.spacepeoples.entity.ResourceType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "PLANET_RESOURCE")
@IdClass(PlanetResourceId.class)
public class PlanetResource extends PlanetResourceId {
    @Id
    @Column(name = "PLANET_ID")
    private String planetId;

    @Id
    @Column(name = "RESOURCE_TYPE")
    private String resourceType;

    @Column(name = "UNITS")
    private int units;

    public ResourceLevel toWeb() {
        return new ResourceLevel(ResourceType.valueOf(resourceType), units);
    }
}
