package de.neebs.spacepeoples.integration.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipRepository extends CrudRepository<Ship, String> {
    Optional<Ship> findByPlanetIdAndReadyIsNotNull(String planetId);

    Iterable<Ship> findByAccountId(String accountId);

    Iterable<Ship> findByPlanetId(String planetId);
}
