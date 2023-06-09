package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetRecycleResourceRepository extends CrudRepository<PlanetRecycleResource, PlanetResourceId> {
    List<PlanetRecycleResource> findByPlanetId(String planetId);
}
