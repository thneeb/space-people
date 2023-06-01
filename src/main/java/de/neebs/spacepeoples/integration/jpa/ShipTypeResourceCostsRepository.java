package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipTypeResourceCostsRepository extends CrudRepository<ShipTypeResourceCosts, ShipTypeResourceCostsId> {
    Iterable<ShipTypeResourceCosts> findByShipTypeIdIn(List<String> shipTypeIds);

    Iterable<ShipTypeResourceCosts> findByShipTypeId(String shipTypeId);

    @Query("SELECT strc FROM ShipTypeResourceCosts strc WHERE strc.shipTypeId IN (SELECT 1 FROM ShipType WHERE accountId = :accountId AND nickname = :shipType)")
    Iterable<ShipTypeResourceCosts> findByShipTypeAndAccountId(String shipType, String accountId);
}
