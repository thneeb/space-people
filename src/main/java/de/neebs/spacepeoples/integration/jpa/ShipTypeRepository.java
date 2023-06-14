package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipTypeRepository extends CrudRepository<ShipType, String> {
    List<ShipType> findByAccountId(String accountId);

    Optional<ShipType> findByPlanetId(String planetId);

    Optional<ShipType> findByAccountIdAndNickname(String accountId, String shipType);

    @Query("SELECT st.nickname AS shipType, COUNT(s) AS count FROM ShipType st JOIN Ship s ON s.shipTypeId = st.shipTypeId " +
            "WHERE s.ready IS NULL AND s.fleetId IS NULL AND s.accountId = :accountId AND s.planetId = :planetId GROUP by st.nickname")
    List<ShipTypeAvailability> countShipsByShipType(String accountId, String planetId);
}
