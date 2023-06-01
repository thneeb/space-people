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
@Table(name = "RESEARCH_TYPE")
public class ResearchType {
    @Id
    @Column(name = "RESEARCH_TYPE")
    private String researchType;

    @Column(name = "CHARACTERISTIC")
    private String characteristic;

    @Column(name = "DURATION_LEVEL_BASE")
    private double durationLevelBase;

    @Column(name = "FACILITY_BASE")
    private double facilityBase;

    @Column(name = "RESEARCH_IN_SECONDS")
    private int researchInSeconds;

    @Column(name = "BUILDING_IN_SECONDS")
    private int buildingInSeconds;

    @Column(name = "SPACE_FIX")
    private int spaceFix;

    @Column(name = "SPACE_PER_LEVEL")
    private int spacePerLevel;

    @Column(name = "BENEFIT_BASIC_VALUE")
    private int benefitBasicValue;

    @Column(name = "BENEFIT_BASE")
    private double benefitBase;

    @Column(name = "BENEFIT_EXPONENT_MODIFIER")
    private double benefitExponentModifier;
}
