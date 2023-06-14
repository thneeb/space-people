package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FleetRepository extends CrudRepository<Fleet, String> {
    Optional<Fleet> findByAccountIdAndNickname(String accountId, String nickname);

    @Query("SELECT SUM(stc.value) FROM Ship s " +
            "JOIN ShipTypeCharacteristic stc ON s.shipTypeId = stc.shipTypeId AND stc.characteristic = 'FUEL' " +
            "WHERE s.fleetId = :fleetId")
    Long sumUpFuelCapacity(String fleetId);

    @Query("SELECT MAX(c.value) FROM Ship s " +
            "JOIN ShipType st ON s.shipTypeId = st.shipTypeId " +
            "JOIN ShipTypeCharacteristic c ON st.shipTypeId = c.shipTypeId AND c.characteristic = 'STABILITY'" +
            "WHERE s.fleetId = :fleetId")
    int maxStability(String fleetId);

    @Query("SELECT SUM(stc.value) FROM Ship s " +
            "JOIN ShipTypeCharacteristic stc ON s.shipTypeId = stc.shipTypeId AND stc.characteristic = 'STORAGE' " +
            "WHERE s.fleetId = :fleetId")
    Long sumUpStorage(String fleetId);

    List<Fleet> findByAccountId(String accountId);

    @Query("SELECT new ShipTypeCount(s.fleetId, st.nickname, COUNT(s)) FROM ShipType st " +
            "JOIN Ship s ON s.shipTypeId = st.shipTypeId " +
            "WHERE s.fleetId IN (:fleetIds) " +
            "GROUP BY s.fleetId, st.nickname")
    List<ShipTypeCount> sumUpShipTypesByFleetIdIn(List<String> fleetIds);

    @Query("SELECT new FleetCharacteristic(s.fleetId, stc.characteristic, SUM(stc.value)) FROM Ship s " +
            "JOIN ShipTypeCharacteristic stc ON stc.shipTypeId = s.shipTypeId " +
            "WHERE s.fleetId In (:fleetIds) " +
            "GROUP BY s.fleetId, stc.characteristic")
    List<FleetCharacteristic> sumUpCharacteristicsByFleetIdIn(List<String> fleetIds);
}
