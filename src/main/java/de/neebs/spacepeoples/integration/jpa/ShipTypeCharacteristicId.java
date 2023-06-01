package de.neebs.spacepeoples.integration.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class ShipTypeCharacteristicId implements Serializable {
    private String shipTypeId;

    private String characteristic;
}
