CREATE TABLE planet_building (
    building_type VARCHAR(40) NOT NULL,
    planet_id VARCHAR(40) NOT NULL,
    level INT8 NOT NULL,
    next_level_update TIMESTAMP,
    PRIMARY KEY (building_type, planet_id),
    FOREIGN KEY (building_type) REFERENCES building_type (building_type),
    FOREIGN KEY (planet_id) REFERENCES planet(planet_id)
);
