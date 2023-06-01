package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingResourceCostsRepository extends CrudRepository<BuildingResourceCosts, BuildingResourceCostsId> {
    Iterable<BuildingResourceCosts> findByBuildingType(String buildingType);
}
