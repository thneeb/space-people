CREATE TABLE fleet_fuel (
    fleet_id VARCHAR(40) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    units INT8 NOT NULL,
    PRIMARY KEY (fleet_id, resource_type),
    FOREIGN KEY (fleet_id) REFERENCES fleet(fleet_id),
    FOREIGN KEY (resource_type) REFERENCES resource_type(resource_type)
);
