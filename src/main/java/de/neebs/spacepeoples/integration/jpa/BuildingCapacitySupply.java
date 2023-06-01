package de.neebs.spacepeoples.integration.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "BUILDING_CAPACITY_SUPPLY")
@IdClass(BuildingCapacitySupplyId.class)
public class BuildingCapacitySupply {
    @Id
    @Column(name = "BUILDING_TYPE")
    private String buildingType;

    @Id
    @Column(name = "CAPACITY_TYPE")
    private String capacityType;

    @Column(name = "BASIC_VALUE")
    private int basicValue;

    @Column(name = "BASE")
    private double base;

    @Column(name = "EXPONENT_MODIFIER")
    private double exponentModifier;
}
