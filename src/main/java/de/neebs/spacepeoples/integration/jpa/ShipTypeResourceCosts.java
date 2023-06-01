package de.neebs.spacepeoples.integration.jpa;

import de.neebs.spacepeoples.entity.ResourceLevel;
import de.neebs.spacepeoples.entity.ResourceType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "SHIP_TYPE_RESOURCE_COSTS")
@IdClass(ShipTypeResourceCostsId.class)
public class ShipTypeResourceCosts {
    @Id
    @Column(name = "SHIP_TYPE_ID")
    private String shipTypeId;

    @Id
    @Column(name = "RESOURCE_TYPE")
    private String resourceType;

    @Column(name = "UNITS")
    private long units;

    public ResourceLevel toWeb() {
        return new ResourceLevel(ResourceType.valueOf(resourceType), units);
    }
}
