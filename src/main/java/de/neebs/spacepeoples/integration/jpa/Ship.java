package de.neebs.spacepeoples.integration.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "SHIP")
public class Ship {
    @Id
    @Column(name = "SHIP_ID")
    private String shipId;

    @Column(name = "SHIP_TYPE_ID")
    private String shipTypeId;

    @Column(name = "ACCOUNT_ID")
    private String accountId;

    @Column(name = "PLANET_ID")
    private String planetId;

    @Column(name = "FLEET_ID")
    private String fleetId;

    @Column(name = "READY")
    private Date ready;

    public de.neebs.spacepeoples.entity.Ship toWeb() {
        de.neebs.spacepeoples.entity.Ship ship = new de.neebs.spacepeoples.entity.Ship();
        return ship;
    }
}
