package de.neebs.spacepeoples.integration.database;

import de.neebs.spacepeoples.entity.ShipPartLevel;
import de.neebs.spacepeoples.entity.ShipPartType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SHIP_TYPE_EQUIPMENT")
@IdClass(ShipTypeEquipmentId.class)
public class ShipTypeEquipment {
    @Id
    @Column(name = "SHIP_PART_TYPE")
    private String shipPartType;

    @Id
    @Column(name = "SHIP_TYPE_ID")
    private String shipTypeId;

    @Column(name = "LEVEL")
    private Integer level;

    public ShipTypeEquipment(String shipPartType, int level) {
        this.shipPartType = shipPartType;
        this.level = level;
    }

    public ShipPartLevel toWeb() {
        ShipPartLevel spl = new ShipPartLevel(ShipPartType.valueOf(shipPartType));
        spl.setLevel(level);
        return spl;
    }
}
