package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResearchResourceResearchCostsRepository extends CrudRepository<ResearchResourceResearchCosts, ResearchResourceId> {
    List<ResearchResourceResearchCosts> findByResearchType(String researchType);
}
