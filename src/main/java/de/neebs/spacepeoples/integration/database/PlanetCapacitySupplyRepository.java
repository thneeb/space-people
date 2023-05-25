package de.neebs.spacepeoples.integration.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanetCapacitySupplyRepository extends CrudRepository<PlanetCapacitySupply, PlanetCapacityId> {
    Iterable<PlanetCapacitySupply> findByPlanetId(String planetId);
}
