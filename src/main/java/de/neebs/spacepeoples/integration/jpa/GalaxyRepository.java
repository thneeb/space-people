package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GalaxyRepository extends CrudRepository<Galaxy, String> {
    Optional<Galaxy> findByNickname(String nickname);
}
