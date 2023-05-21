package de.neebs.spacepeoples.integration.database;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanetRepository extends CrudRepository<Planet, String> {
    @Query("SELECT p FROM Planet p WHERE p.galaxyId IN (SELECT g.galaxyId FROM Galaxy g WHERE g.nickname = :universeName)")
    Iterable<Planet> findByUniverseName(String universeName);

    @Query("SELECT p " +
            "FROM Planet p " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 " +
            "FROM Planet pl " +
            "JOIN Account a ON pl.accountId = a.accountId " +
            "WHERE pl.x = p.x AND pl.y = p.y AND pl.z = p.z AND pl.galaxyId = p.galaxyId" +
            ") AND p.orbit = 'C'")
    Iterable<Planet> findFreeSolarSystem();

    Iterable<Planet> findByAccountId(String accountId);

    Optional<Planet> findByGalaxyIdAndXAndYAndZAndOrbit(String galaxyId, int x, int y, int c, char orbit);
}
