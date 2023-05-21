CREATE TABLE planet_resource (
    planet_id VARCHAR(40) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    units INT8 NOT NULL,
    productivity INT8 NOT NULL,
    next_update TIMESTAMP,
    last_update TIMESTAMP,
    PRIMARY KEY (planet_id, resource_type),
    FOREIGN KEY (planet_id) REFERENCES planet(planet_id),
    FOREIGN KEY (resource_type) REFERENCES resource_type(resource_type)
);
