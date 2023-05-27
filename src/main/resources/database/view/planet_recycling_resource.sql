CREATE OR REPLACE VIEW planet_resource_recycling AS
SELECT pr.planet_id, pr.resource_type,
       LEAST(brp.basic_value * brp.base ^ (pb.level + brp.exponent_modifier)/ 3600 * EXTRACT(EPOCH FROM (NOW()-COALESCE(pr.last_update, NOW()))), pr.units, GREATEST(COALESCE(psa.capacity_supply, 0) - COALESCE(psu.storage_used), 0)) AS additional_units,
       NOW() + interval '1 hour' * CEILING(brp.basic_value * brp.base ^ (pb.level + brp.exponent_modifier) / 3600) / (brp.basic_value * brp.base ^ (pb.level + brp.exponent_modifier)) AS next_update
FROM planet_recycle_resource pr
JOIN building_resource_recycling brp ON brp.resource_type = pr.resource_type
JOIN planet_building pb ON brp.building_type = pb.building_type AND pr.planet_id = pb.planet_id
LEFT JOIN planet_capacity_supply psa ON psa.planet_id = pr.planet_id AND psa.capacity_type = 'STORAGE'
LEFT JOIN planet_storage_used psu ON psu.planet_id = pr.planet_id
;
