package de.neebs.spacepeoples.integration.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanetCapacityUsedRepository extends CrudRepository<PlanetCapacityUsed, PlanetCapacityId> {
    Iterable<PlanetCapacityUsed> findByPlanetId(String planetId);
}