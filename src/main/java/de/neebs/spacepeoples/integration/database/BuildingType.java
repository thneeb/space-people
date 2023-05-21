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
@Table(name = "BUILDING_TYPE")
public class BuildingType {
    @Id
    @Column(name = "BUILDING_TYPE")
    private String buildingType;

    @Column(name = "DURATION_BASIS")
    private double durationBasis;

    @Column(name = "DURATION_IN_SECONDS")
    private int durationInSeconds;
}
