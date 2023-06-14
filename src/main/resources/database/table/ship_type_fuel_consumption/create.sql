CREATE TABLE ship_type_fuel_consumption (
    ship_type_id VARCHAR(40) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    units INT8 NOT NULL,
    PRIMARY KEY (ship_type_id, resource_type),
    FOREIGN KEY (ship_type_id) REFERENCES ship_type(ship_type_id),
    FOREIGN KEY (resource_type) REFERENCES resource_type(resource_type)
);
