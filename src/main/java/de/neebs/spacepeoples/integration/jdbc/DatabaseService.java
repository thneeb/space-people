package de.neebs.spacepeoples.integration.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

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
                "SET units = units + LEAST(prp.additional_units, GREATEST(COALESCE(psa.capacity_supply, 0) - COALESCE(psu.storage_used, 0), 0)), next_update = prp.next_update, last_update = NOW()\n" +
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
                "AND r.additional_units > 0\n" +
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
        return jdbcTemplate.update("INSERT INTO research_level (account_id, research_type, level) SELECT ?, research_type, 0 FROM research_type", accountId);
    }

    public int finishResearches() {
        return jdbcTemplate.update("UPDATE research_level SET level = level + 1, next_level_update = NULL, planet_id = NULL WHERE next_level_update <= NOW()");
    }

    public List<ResearchPrerequisite> findBuildingPrerequisites(String accountId, String researchType) {
        return jdbcTemplate.query("SELECT acc.account_id, rtbp.research_type, rtbp.prerequisite, rtbp.level AS needed_level, COALESCE(mbtl.level, 0) AS actual_level\n" +
                "FROM research_type_building_prerequisite rtbp\n" +
                "CROSS JOIN account acc\n" +
                "LEFT JOIN max_building_type_level mbtl ON rtbp.prerequisite = mbtl.building_type AND acc.account_id = mbtl.account_id\n" +
                "WHERE acc.account_id = ? AND rtbp.research_type = ?\n" +
                "AND rtbp.level > COALESCE(mbtl.level, 0)" , new Object[] { accountId, researchType }, new int[] {Types.VARCHAR, Types.VARCHAR}, (rs, rowNum) -> {
                    ResearchPrerequisite rp = new ResearchPrerequisite();
                    rp.setPrerequisite(rs.getString("prerequisite"));
                    rp.setNeededLevel(rs.getInt("needed_level"));
                    rp.setActualLevel(rs.getInt("actual_level"));
                    return rp;
                });
    }

    public List<ResearchPrerequisite> findResearchPrerequisites(String accountId, String researchType) {
        return jdbcTemplate.query("SELECT acc.account_id, rtbp.research_type, rtbp.prerequisite, rtbp.level AS needed_level, COALESCE(rl.level, 0) AS actual_level\n" +
                "FROM research_type_research_prerequisite rtbp\n" +
                "CROSS JOIN account acc\n" +
                "LEFT JOIN research_level rl ON rtbp.prerequisite = rl.research_type AND acc.account_id = rl.account_id\n" +
                "WHERE acc.account_id = ? AND rtbp.research_type = ?\n" +
                "AND rtbp.level > COALESCE(rl.level, 0)" , new Object[] { accountId, researchType }, new int[] {Types.VARCHAR, Types.VARCHAR}, (rs, rowNum) -> {
            ResearchPrerequisite rp = new ResearchPrerequisite();
            rp.setPrerequisite(rs.getString("prerequisite"));
            rp.setNeededLevel(rs.getInt("needed_level"));
            rp.setActualLevel(rs.getInt("actual_level"));
            return rp;
        });
    }

    public int assignShipsToFleet(String fleetId, String planetId, String shipType, Long count) {
        return jdbcTemplate.update("UPDATE ship " +
                "SET fleet_id = ? " +
                "WHERE ship_id IN (" +
                "SELECT s.ship_id FROM ship s JOIN ship_type st ON s.ship_type_id = st.ship_type_id " +
                "WHERE s.ready IS NULL AND s.fleet_id IS NULL " +
                "AND s.planet_id = ? AND st.nickname = ? LIMIT ?" +
                ")", fleetId, planetId, shipType, count);
    }

    public int finishShipTypes() {
        return jdbcTemplate.update("UPDATE ship_type SET ready = NULL, planet_id = NULL WHERE ready <= NOW()");
    }

    public int finishShips() {
        return jdbcTemplate.update("UPDATE ship SET ready = NULL WHERE ready <= NOW()");
    }

    public int finishFleetActions() {
        return jdbcTemplate.update("UPDATE fleet SET next_status_update = NULL WHERE fleet.next_status_update <= NOW()");
    }
}
