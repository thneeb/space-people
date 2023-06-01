package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ShipTypeEquipmentRepository extends CrudRepository<ShipTypeEquipment, ShipTypeEquipmentId> {
    Iterable<ShipTypeEquipment> findAllByShipTypeId(String shipTypeId);

    Iterable<ShipTypeEquipment> findAllByShipTypeIdIn(Collection<String> shipTypeIds);
}
