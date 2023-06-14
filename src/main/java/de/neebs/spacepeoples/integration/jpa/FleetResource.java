package de.neebs.spacepeoples.integration.jpa;

import de.neebs.spacepeoples.entity.ResourceLevel;
import de.neebs.spacepeoples.entity.ResourceType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "FLEET_RESOURCE")
@IdClass(FleetResourceId.class)
public class FleetResource {
    @Id
    @Column(name = "FLEET_ID")
    private String fleetId;

    @Id
    @Column(name = "RESOURCE_TYPE")
    private String resourceType;

    @Column(name = "UNITS")
    private long units;

    public ResourceLevel toWeb() {
        return new ResourceLevel(ResourceType.valueOf(resourceType), units);
    }
}
