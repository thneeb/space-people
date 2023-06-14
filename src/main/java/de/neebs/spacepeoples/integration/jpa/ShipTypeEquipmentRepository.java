package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ShipTypeEquipmentRepository extends CrudRepository<ShipTypeEquipment, ShipTypeEquipmentId> {
    List<ShipTypeEquipment> findAllByShipTypeId(String shipTypeId);

    List<ShipTypeEquipment> findAllByShipTypeIdIn(Collection<String> shipTypeIds);
}
