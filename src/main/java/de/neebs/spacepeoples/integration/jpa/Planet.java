package de.neebs.spacepeoples.integration.jpa;

import de.neebs.spacepeoples.entity.GalacticPosition;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "planet")
public class Planet {
    @Id
    @Column(name = "PLANET_ID")
    private String planetId;

    @Column(name = "GALAXY_ID")
    private String galaxyId;

    @Transient
    private String galaxyName;

    @Column(name = "COORDINATE_X")
    private int x;

    @Column(name = "COORDINATE_Y")
    private int y;

    @Column(name = "COORDINATE_Z")
    private int z;

    @Column(name = "ORBIT")
    private char orbit;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ACCOUNT_ID")
    private String accountId;

    public de.neebs.spacepeoples.entity.Planet toWeb() {
        de.neebs.spacepeoples.entity.Planet planet = new de.neebs.spacepeoples.entity.Planet(
                galaxyName + "-" + x + "-" + y + "-" + z + "-" + orbit, new GalacticPosition(x, y, z, String.valueOf(orbit)));
        planet.setPlanetName(name);
        planet.setGalaxyName(galaxyName);
        return planet;
    }
}
