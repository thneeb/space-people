package de.neebs.spacepeoples.integration.database;

import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.persistence.*;

@Getter
@Service
@Entity
@Table(name = "PLANET_RESOURCE")
@IdClass(PlanetResourceId.class)
public class PlanetResource extends PlanetResourceId {
    @Id
    @Column(name = "PLANET_ID")
    private String planet_id;

    @Id
    @Column(name = "RESOURCE_TYPE")
    private String resourceType;

    @Column(name = "UNITS")
    private int units;
}
