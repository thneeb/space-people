package de.neebs.spacepeoples.integration.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipTypeEquipmentRepository extends CrudRepository<ShipTypeEquipment, ShipTypeEquipmentId> {
    Iterable<ShipTypeEquipment> findAllByShipTypeIdIn(Iterable<String> shipTypeIds);
}
