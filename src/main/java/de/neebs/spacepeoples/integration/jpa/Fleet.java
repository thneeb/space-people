package de.neebs.spacepeoples.integration.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
}
