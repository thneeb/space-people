package de.neebs.spacepeoples.integration.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ShipTypeEquipmentRepository extends CrudRepository<ShipTypeEquipment, ShipTypeEquipmentId> {
    Iterable<ShipTypeEquipment> findAllByShipTypeIdIn(Collection<String> shipTypeIds);
}
