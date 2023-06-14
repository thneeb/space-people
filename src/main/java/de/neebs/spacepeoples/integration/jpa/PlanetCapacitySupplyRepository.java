package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetCapacitySupplyRepository extends CrudRepository<PlanetCapacitySupply, PlanetCapacityId> {
    List<PlanetCapacitySupply> findByPlanetId(String planetId);
}
