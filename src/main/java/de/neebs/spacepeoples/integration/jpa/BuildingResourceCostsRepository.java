package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingResourceCostsRepository extends CrudRepository<BuildingResourceCosts, BuildingResourceCostsId> {
    List<BuildingResourceCosts> findByBuildingType(String buildingType);
}
