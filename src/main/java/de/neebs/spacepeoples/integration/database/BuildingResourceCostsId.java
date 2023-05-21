package de.neebs.spacepeoples.integration.database;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class BuildingResourceCostsId implements Serializable {
    private String buildingType;

    private String resourceType;
}
