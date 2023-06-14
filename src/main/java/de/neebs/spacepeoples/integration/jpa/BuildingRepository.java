package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends CrudRepository<Building, BuildingId> {
    List<Building> findByPlanetId(String planetId);
}
