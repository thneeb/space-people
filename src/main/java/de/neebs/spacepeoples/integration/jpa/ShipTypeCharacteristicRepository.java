package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipTypeCharacteristicRepository extends CrudRepository<ShipTypeCharacteristic, ShipTypeCharacteristicId> {
    Iterable<ShipTypeCharacteristic> findByShipTypeIdIn(List<String> shipTypeIds);
}
