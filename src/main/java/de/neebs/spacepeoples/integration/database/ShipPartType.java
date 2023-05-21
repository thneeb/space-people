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
@Table(name = "SHIP_PART_TYPE")
public class ShipPartType {
    @Id
    @Column(name = "SHIP_PART_TYPE")
    private String shipPartType;

    @Column(name = "DURATION_BASIS")
    private double durationBasis;

    @Column(name = "DURATION_IN_SECONDS")
    private int durationInSeconds;

    @Column(name = "SPACE_FIX")
    private int spaceFix;

    @Column(name = "SPACE_PER_LEVEL")
    private int spacePerLevel;
}
