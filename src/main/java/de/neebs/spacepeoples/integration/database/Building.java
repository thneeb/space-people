package de.neebs.spacepeoples.integration.database;

import de.neebs.spacepeoples.entity.BuildingStatus;
import de.neebs.spacepeoples.entity.BuildingType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "PLANET_BUILDING")
@IdClass(BuildingId.class)
public class Building {
    @Id
    @Column(name = "PLANET_ID")
    private String planetId;
    @Id
    @Column(name = "BUILDING_TYPE")
    private String buildingType;

    @Column(name = "LEVEL")
    private int level;

    @Column(name = "NEXT_LEVEL_UPDATE")
    private Date nextLevelUpdate;

    public de.neebs.spacepeoples.entity.Building toWeb() {
        de.neebs.spacepeoples.entity.Building building = new de.neebs.spacepeoples.entity.Building();
        building.setBuildingType(BuildingType.valueOf(buildingType));
        building.setLevel(level);
        building.setNextLevelUpdate(nextLevelUpdate);
        return building;
    }
}
