package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FleetResourceRepository extends CrudRepository<FleetResource, FleetResourceId> {
    List<FleetResource> findByFleetId(String fleetId);
}
