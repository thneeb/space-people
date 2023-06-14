package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResearchLevelRepository extends CrudRepository<ResearchLevel, ResearchLevelId> {
    List<ResearchLevel> findByAccountId(String accountId);
}
