CREATE OR REPLACE VIEW planet_storage_used AS
SELECT planet_id, SUM(units) AS storage_used
FROM planet_resource
GROUP BY planet_id
;
