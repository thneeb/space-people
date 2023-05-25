package de.neebs.spacepeoples.integration.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetRecycleResourceRepository extends CrudRepository<PlanetRecycleResource, PlanetResourceId> {
    Iterable<PlanetRecycleResource> findByPlanetId(String planetId);
}
