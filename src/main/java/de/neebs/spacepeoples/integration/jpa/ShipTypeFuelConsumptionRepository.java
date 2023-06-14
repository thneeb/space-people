package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipTypeFuelConsumptionRepository extends CrudRepository<ShipTypeFuelConsumption, ShipTypeResourceId> {
    List<ShipTypeFuelConsumption> findByShipTypeIdIn(List<String> shipTypeIds);

    @Query("SELECT new ShipTypeFuelConsumption('does not matter', stfc.resourceType, SUM(stfc.units)) FROM ShipTypeFuelConsumption stfc " +
            "JOIN ShipType st ON stfc.shipTypeId = st.shipTypeId " +
            "JOIN Ship s ON s.shipTypeId = st.shipTypeId " +
            "WHERE s.fleetId = :fleetId " +
            "GROUP BY stfc.resourceType")
    List<ShipTypeFuelConsumption> sumUpFuelConsumption(String fleetId);
}
