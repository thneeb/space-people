package de.neebs.spacepeoples.integration.jpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FullFleet {
    private Fleet fleet;
    private List<FleetFuel> fleetFuels;
    private List<ShipTypeCount> fleetShipTypeCounts;
    private List<FleetCharacteristic> fleetCharacteristics;
}
