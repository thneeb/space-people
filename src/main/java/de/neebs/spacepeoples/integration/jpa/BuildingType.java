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
@Table(name = "BUILDING_TYPE")
public class BuildingType {
    @Id
    @Column(name = "BUILDING_TYPE")
    private String buildingType;

    @Column(name = "DURATION_IN_SECONDS")
    private int durationInSeconds;

    @Column(name = "LEVEL_BASE")
    private double levelBase;

    @Column(name = "BUILDING_YARD_BASE")
    private double buildingYardBase;
}
