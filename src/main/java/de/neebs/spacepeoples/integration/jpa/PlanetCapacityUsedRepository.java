package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetCapacityUsedRepository extends CrudRepository<PlanetCapacityUsed, PlanetCapacityId> {
    List<PlanetCapacityUsed> findByPlanetId(String planetId);
}
