package de.neebs.spacepeoples.integration.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipTypeRepository extends CrudRepository<ShipType, String> {
    Iterable<ShipType> findByAccountId(String accountId);
}
