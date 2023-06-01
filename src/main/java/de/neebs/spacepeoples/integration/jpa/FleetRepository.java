package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FleetRepository extends CrudRepository<Fleet, String> {
    Optional<Fleet> findByAccountIdAndNickname(String accountId, String nickname);

    @Query("SELECT SUM(stc.value) FROM Ship s JOIN ShipTypeCharacteristic stc ON s.shipTypeId = stc.shipTypeId AND stc.characteristic = 'FUEL' WHERE s.fleetId = :fleetId")
    Long sumUpFuelCapacity(String fleetId);

    @Query("SELECT SUM(st.oxygenConsumptionPerHour) AS oxygen, SUM(st.hydrogenConsumptionPerHour) AS hydrogen " +
            "FROM Ship s JOIN ShipType st ON s.shipTypeId = st.shipTypeId WHERE s.fleetId = :fleetId")
    Fuel sumUpFuelConsumptionPerHour(String fleetId);

    @Query("SELECT SUM(oxy.units) AS oxygen, SUM(hydro.units) AS hydrogen FROM Fleet f " +
            "LEFT JOIN FleetFuel oxy ON f.fleetId = oxy.fleetId AND oxy.resourceType = 'OXYGEN' " +
            "LEFT JOIN FleetFuel hydro ON f.fleetId = hydro.fleetId AND hydro.resourceType = 'HYDROGEN' " +
            "WHERE f.fleetId = :fleetId")
    Fuel sumUpAvailableFuel(String fleetId);
}
