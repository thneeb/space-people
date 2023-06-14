package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanetResourceRepository extends CrudRepository<PlanetResource, PlanetResourceId> {
    List<PlanetResource> findByPlanetId(String planetId);
}
