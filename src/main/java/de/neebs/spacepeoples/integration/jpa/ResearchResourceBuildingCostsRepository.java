package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResearchResourceBuildingCostsRepository extends CrudRepository<ResearchResourceBuildingCosts, ResearchResourceId> {
    List<ResearchResourceBuildingCosts> findByResearchType(String researchType);
}
