CREATE OR REPLACE VIEW planet_resource_production AS
SELECT pr.planet_id, pr.resource_type,
       brp.basic_value * pr.productivity / 100 * brp.base ^ (pb.level + brp.exponent_modifier)/ 3600 * EXTRACT(EPOCH FROM (NOW()-last_update)) AS additional_units,
       NOW() + interval '1 hour' * CEILING(brp.basic_value * pr.productivity / 100 * brp.base ^ (pb.level + brp.exponent_modifier) / 3600) / (brp.basic_value * pr.productivity / 100 * brp.base ^ (pb.level + brp.exponent_modifier)) AS next_update
FROM planet_resource pr
JOIN building_resource_production brp ON brp.resource_type = pr.resource_type
JOIN planet_building pb ON brp.building_type = pb.building_type AND pr.planet_id = pb.planet_id
;
