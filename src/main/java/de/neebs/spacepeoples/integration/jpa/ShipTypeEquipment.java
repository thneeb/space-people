package de.neebs.spacepeoples.integration.jpa;

import de.neebs.spacepeoples.entity.ResearchType;
import de.neebs.spacepeoples.entity.ShipPartLevel;
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
    @Column(name = "RESEARCH_TYPE")
    private String researchType;

    @Id
    @Column(name = "SHIP_TYPE_ID")
    private String shipTypeId;

    @Column(name = "LEVEL")
    private Integer level;

    public ShipTypeEquipment(String researchType, int level) {
        this.researchType = researchType;
        this.level = level;
    }

    public ShipPartLevel toWeb() {
        ShipPartLevel spl = new ShipPartLevel(ResearchType.valueOf(researchType));
        spl.setLevel(level);
        return spl;
    }
}
