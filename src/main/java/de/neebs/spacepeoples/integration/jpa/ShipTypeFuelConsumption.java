package de.neebs.spacepeoples.integration.jpa;

import de.neebs.spacepeoples.entity.ResourceLevel;
import de.neebs.spacepeoples.entity.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SHIP_TYPE_FUEL_CONSUMPTION")
@IdClass(ShipTypeResourceId.class)
public class ShipTypeFuelConsumption {
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
