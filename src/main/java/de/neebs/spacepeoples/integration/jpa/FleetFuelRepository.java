package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FleetFuelRepository extends CrudRepository<FleetFuel, FleetResourceId> {
    List<FleetFuel> findByFleetId(String fleetId);

    List<FleetFuel> findByFleetIdIn(List<String> fleetIds);
}
