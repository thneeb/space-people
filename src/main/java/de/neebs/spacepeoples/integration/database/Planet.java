package de.neebs.spacepeoples.integration.database;

import de.neebs.spacepeoples.entity.GalacticPosition;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Planet {
    @Id
    @Column(name = "PLANET_ID")
    private String planetId;

    @Column(name = "GALAXY_ID")
    private String galaxyId;

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
        de.neebs.spacepeoples.entity.Planet planet = new de.neebs.spacepeoples.entity.Planet();
        planet.setCoordinates(new GalacticPosition(x, y, z, String.valueOf(orbit)));
        planet.setNickname(name);
        return planet;
    }
}
