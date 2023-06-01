package de.neebs.spacepeoples.integration.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "RESEARCH_RESOURCE_BUILDING_COSTS")
@IdClass(ResearchResourceId.class)
public class ResearchResourceBuildingCosts {
    @Id
    @Column(name = "RESEARCH_TYPE")
    private String researchType;

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
