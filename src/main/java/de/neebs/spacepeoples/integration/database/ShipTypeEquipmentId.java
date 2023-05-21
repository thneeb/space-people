package de.neebs.spacepeoples.integration.database;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ShipTypeEquipmentId implements Serializable {
    private String shipPartType;

    private String shipTypeId;
}
