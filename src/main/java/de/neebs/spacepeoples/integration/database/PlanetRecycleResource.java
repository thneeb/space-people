package de.neebs.spacepeoples.integration.database;

import de.neebs.spacepeoples.entity.ResourceLevel;
import de.neebs.spacepeoples.entity.ResourceType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

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

    @Column(name = "NEXT_UPDATE")
    private Date nextUpdate;

    public ResourceLevel toWeb() {
        return new ResourceLevel(ResourceType.valueOf(resourceType), units);
    }
}
