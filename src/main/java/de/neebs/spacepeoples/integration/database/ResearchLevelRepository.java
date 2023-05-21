package de.neebs.spacepeoples.integration.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResearchLevelRepository extends CrudRepository<ResearchLevel, ResearchLevelId> {
    Iterable<ResearchLevel> findByAccountId(String accountId);
}
