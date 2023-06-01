package de.neebs.spacepeoples.integration.jpa;

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
    private List<ShipTypeCharacteristic> characteristics;
    private List<ShipTypeResourceCosts> buildingResources;
    private List<ShipTypeResourceCosts> researchResources;

    public de.neebs.spacepeoples.entity.ShipType toWeb() {
        de.neebs.spacepeoples.entity.ShipType st = new de.neebs.spacepeoples.entity.ShipType();
        st.setNickname(shipType.getNickname());
        st.setBuildingTimeInSeconds(shipType.getBuildingTimeInSeconds());
        st.setResearchTimeInSeconds(shipType.getResearchTimeInSeconds());
        st.setHydrogenConsumptionPerHour(shipType.getHydrogenConsumptionPerHour());
        st.setOxygenConsumptionPerHour(shipType.getOxygenConsumptionPerHour());
        st.setReady(shipType.getReady());
        st.setCharacteristics(characteristics.stream().map(ShipTypeCharacteristic::toWeb).collect(Collectors.toList()));
        st.setEquipments(equipments.stream().map(ShipTypeEquipment::toWeb).collect(Collectors.toList()));
        st.setBuildingCosts(buildingResources.stream().map(ShipTypeResourceCosts::toWeb).collect(Collectors.toList()));
        if (researchResources != null) {
            st.setResearchCosts(researchResources.stream().map(ShipTypeResourceCosts::toWeb).collect(Collectors.toList()));
        }
        return st;
    }
}
