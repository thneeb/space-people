CREATE OR REPLACE VIEW planet_capacity_supply AS
SELECT pb.planet_id, ct.capacity_type, SUM(bcs.basic_value * bcs.base ^ (pb.level + bcs.exponent_modifier)) AS capacity_supply
FROM capacity_type ct
JOIN building_capacity_supply bcs ON bcs.capacity_type = ct.capacity_type
JOIN planet_building pb ON bcs.building_type = pb.building_type AND pb.level > 0
GROUP BY pb.planet_id, ct.capacity_type
;
