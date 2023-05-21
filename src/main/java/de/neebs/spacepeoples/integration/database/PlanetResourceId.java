package de.neebs.spacepeoples.integration.database;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class PlanetResourceId implements Serializable {
    private String planetId;

    private String resourceType;
}
