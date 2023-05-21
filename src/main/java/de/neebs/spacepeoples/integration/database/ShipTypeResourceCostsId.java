package de.neebs.spacepeoples.integration.database;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ShipTypeResourceCostsId implements Serializable {
    private String shipTypeId;

    private String resourceType;
}
