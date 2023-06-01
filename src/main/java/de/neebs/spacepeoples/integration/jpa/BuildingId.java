package de.neebs.spacepeoples.integration.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class BuildingId implements Serializable {
    private String planetId;

    private String buildingType;
}
