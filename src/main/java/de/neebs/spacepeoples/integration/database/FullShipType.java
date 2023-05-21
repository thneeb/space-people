package de.neebs.spacepeoples.integration.database;

import de.neebs.spacepeoples.entity.ResourceLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class FullShipType {
    private ShipType shipType;
    private List<ShipTypeEquipment> equipments;
    private List<ShipTypeResourceCosts> resources;

    public de.neebs.spacepeoples.entity.ShipType toWeb() {
        de.neebs.spacepeoples.entity.ShipType st = new de.neebs.spacepeoples.entity.ShipType();
        st.setNickname(shipType.getNickname());
        st.setAcceleration(shipType.getAcceleration());
        st.setArmour(shipType.getArmour());
        st.setAttack(shipType.getAttack());
        st.setCargo(shipType.getCargoUnits());
        st.setFuel(shipType.getFuelUnits());
        st.setStability(shipType.getStability());
        st.setHydrogenConsumptionPerHour(shipType.getHydrogenConsumptionPerHour());
        st.setEquipments(equipments.stream().map(ShipTypeEquipment::toWeb).collect(Collectors.toList()));
        st.setResources(resources.stream().map(ShipTypeResourceCosts::toWeb).collect(Collectors.toList()));
        return st;
    }
}
