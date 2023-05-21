package de.neebs.spacepeoples.integration.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanetResourceRepository extends CrudRepository<PlanetResource, PlanetResourceId> {
    Iterable<PlanetResource> findByPlanetId(String planetId);
}
