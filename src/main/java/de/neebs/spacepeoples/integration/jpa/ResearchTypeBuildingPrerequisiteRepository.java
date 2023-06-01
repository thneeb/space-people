package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResearchTypeBuildingPrerequisiteRepository extends CrudRepository<ResearchTypeBuildingPrerequisite, ResearchTypePrerequisiteId> {
}
