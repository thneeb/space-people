package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanetRecycleResourceRepository extends CrudRepository<PlanetRecycleResource, PlanetResourceId> {
    Iterable<PlanetRecycleResource> findByPlanetId(String planetId);
}
