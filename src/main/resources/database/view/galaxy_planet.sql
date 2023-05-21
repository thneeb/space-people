CREATE OR REPLACE VIEW galaxy_planet AS
SELECT g.nickname AS galaxyname, p.coordinate_x, p.coordinate_y, p.coordinate_z, p.orbit, p.name, p.account_id, p.planet_id, g.galaxy_id
FROM galaxy g
JOIN planet p on g.galaxy_id = p.galaxy_id
;