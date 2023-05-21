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
                "WHERE production_per_hour IS NOT NULL " +
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
        return jdbcTemplate.update("MERGE INTO planet_resource re\n" +
                "USING (\n" +
                "\tSELECT pr.planet_id, pr.resource_type,\n" +
                "\t\tbrp.production_per_hour * pr.productivity / 100 * brp.production_basis ^ pb.level / 3600 * EXTRACT(EPOCH FROM (NOW()-last_update)) AS additional_units, \n" +
                "\t\tNOW() + interval '1 hour' * CEILING(brp.production_per_hour * pr.productivity / 100 * brp.production_basis ^ pb.level / 3600) / (brp.production_per_hour * pr.productivity / 100 * brp.production_basis ^ pb.level) AS next_update\n" +
                "\tFROM planet_resource pr\n" +
                "\tJOIN building_resource_production brp ON brp.resource_type = pr.resource_type\n" +
                "\tJOIN planet_building pb ON brp.building_type = pb.building_type AND pr.planet_id = pb.planet_id\n" +
                ") pr ON (pr.planet_id = re.planet_id AND pr.resource_type = re.resource_type AND re.next_update <= NOW())\n" +
                "WHEN MATCHED THEN\n" +
                "UPDATE SET units = units + additional_units, next_update = pr.next_update, last_update = NOW()");
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
