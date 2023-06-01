package de.neebs.spacepeoples.integration.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class BuildingCapacitySupplyId implements Serializable {
    private String buildingType;

    private String capacityType;
}
