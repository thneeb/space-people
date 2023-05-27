package de.neebs.spacepeoples.integration.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseService {
    private final JdbcTemplate jdbcTemplate;

    public void createInitialResourceBuildings(String planetId) {
        jdbcTemplate.update("INSERT INTO planet_building (building_type, planet_id, level) " +
                "SELECT building_type, ?, 1 " +
                "FROM building_resource_production " +
                "GROUP BY building_type", planetId);
    }

    public void generateResources(String galaxyId) {
        jdbcTemplate.update("INSERT INTO planet_resource (planet_id, resource_type, units, productivity, next_update, last_update) " +
                "SELECT p.planet_id, rt.resource_type, 0, FLOOR(RANDOM() * 99 + 1)::int, NOW(), NOW() " +
                "FROM planet p " +
                "CROSS JOIN resource_type rt " +
                "WHERE p.galaxy_id = ?", galaxyId);
    }

    public int executeResourceProduction() {
        return jdbcTemplate.update("UPDATE planet_resource re\n" +
                "SET units = units + LEAST(prp.additional_units, GREATEST(COALESCE(psa.capacity_supply, 0) - COALESCE(psu.storage_used), 0)), next_update = prp.next_update, last_update = NOW()\n" +
                "FROM planet_resource_production prp\n" +
                "LEFT JOIN planet_capacity_supply psa ON psa.planet_id = prp.planet_id AND psa.capacity_type = 'STORAGE'\n" +
                "LEFT JOIN planet_storage_used psu ON psu.planet_id = prp.planet_id\n" +
                "WHERE prp.planet_id = re.planet_id\n" +
                "AND prp.resource_type = re.resource_type \n" +
                "AND COALESCE(re.next_update, NOW()) <= NOW()");
    }

    public int executeResourceRecycling() {
        return jdbcTemplate.update("WITH resources AS (\n" +
                "SELECT * FROM planet_resource_recycling\n" +
                "), recycling AS (\n" +
                "UPDATE planet_recycle_resource prr \n" +
                "SET next_update = r.next_update, last_update = NOW(), units = units - r.additional_units\n" +
                "FROM resources r\n" +
                "WHERE COALESCE(prr.next_update, NOW()) <= NOW()\n" +
                "AND r.planet_id = prr.planet_id\n" +
                "AND r.resource_type = prr.resource_type\n" +
                "RETURNING prr.planet_id, prr.resource_type, r.additional_units\n" +
                ") \n" +
                "UPDATE planet_resource pr\n" +
                "SET units = units + rec.additional_units\n" +
                "FROM recycling rec\n" +
                "WHERE rec.planet_id = pr.planet_id\n" +
                "AND rec.resource_type = pr.resource_type");
    }

    public int finishBuildings() {
        return jdbcTemplate.update("UPDATE planet_building SET level = level + 1, next_level_update = NULL WHERE next_level_update <= NOW()");
    }

    public int createInitialResearchStatus(String accountId) {
        return jdbcTemplate.update("INSERT INTO research_level (account_id, ship_part_type, level) SELECT ?, ship_part_type, 0 FROM ship_part_type", accountId);
    }

    public int finishResearches() {
        return jdbcTemplate.update("UPDATE research_level SET level = level + 1, next_level_update = NULL, planet_id = NULL WHERE next_level_update <= NOW()");
    }
}
