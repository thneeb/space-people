package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResearchResourceResearchCostsRepository extends CrudRepository<ResearchResourceResearchCosts, ResearchResourceId> {
    Iterable<ResearchResourceResearchCosts> findByResearchType(String researchType);
}
