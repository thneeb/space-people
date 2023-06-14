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
@Table(name = "FLEET")
public class Fleet {
    @Id
    @Column(name = "FLEET_ID")
    private String fleetId;

    @Column(name = "NICKNAME")
    private String nickname;

    @Column(name = "ACCOUNT_ID")
    private String accountId;

    @Column(name = "FLEET_STATUS")
    private String status;

    @Column(name = "PLANET_ID")
    private String planetId;

    @Column(name = "NEXT_STATUS_UPDATE")
    private Date nextStatusUpdate;

    public de.neebs.spacepeoples.entity.Fleet toWeb() {
        de.neebs.spacepeoples.entity.Fleet fleet = new de.neebs.spacepeoples.entity.Fleet();
        fleet.setNickname(nickname);
        fleet.setStatus(status);
        fleet.setArivialTime(nextStatusUpdate);
        return fleet;
    }
}
