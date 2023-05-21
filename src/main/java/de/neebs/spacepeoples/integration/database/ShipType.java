package de.neebs.spacepeoples.integration.database;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "SHIP_TYPE")
public class ShipType {
    @Id
    @Column(name = "SHIP_TYPE_ID")
    private String shipTypeId;

    @Column(name = "NICKNAME")
    private String nickname;

    @Column(name = "ACCOUNT_ID")
    private String accountId;

    @Column(name = "FUEL_UNITS")
    private int fuelUnits;

    @Column(name = "STABILITY")
    private int stability;

    @Column(name = "ARMOUR")
    private int armour;

    @Column(name = "ATTACK")
    private int attack;

    @Column(name = "CARGO_UNITS")
    private int cargoUnits;

    @Column(name = "ACCELERATION")
    private int acceleration;

    @Column(name = "HYDROGEN_CONSUMPTION_PER_HOUR")
    private int hydrogenConsumptionPerHour;
}
