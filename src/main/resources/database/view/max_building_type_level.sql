CREATE OR REPLACE VIEW max_building_type_level AS
SELECT p.account_id, pb.building_type, MAX(pb.level) AS level
FROM planet p
JOIN planet_building pb ON p.planet_id = pb.planet_id
GROUP BY p.account_id, pb.building_type
;
