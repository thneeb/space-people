package de.neebs.spacepeoples.integration.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "BUILDING_RESOURCE_COSTS")
@IdClass(BuildingResourceCostsId.class)
public class BuildingResourceCosts extends BuildingResourceCostsId {
    @Id
    @Column(name = "BUILDING_TYPE")
    private String buildingType;

    @Id
    @Column(name = "RESOURCE_TYPE")
    private String resourceType;

    @Column(name = "BASIC_VALUE")
    private int basicValue;

    @Column(name = "BASE")
    private double base;

    @Column(name = "EXPONENT_MODIFIER")
    private double exponentModifier;
}
