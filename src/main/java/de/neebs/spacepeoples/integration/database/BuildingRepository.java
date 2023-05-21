package de.neebs.spacepeoples.integration.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends CrudRepository<Building, BuildingId> {
    Iterable<Building> findByPlanetId(String planetId);
}
