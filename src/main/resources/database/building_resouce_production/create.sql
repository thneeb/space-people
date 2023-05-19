CREATE TABLE building_resource_production (
    building_type VARCHAR(40) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    production_per_hour INT8 NOT NULL,
    level_basis DECIMAL(8,6) NOT NULL,
    PRIMARY KEY (building_type, resource_type),
    FOREIGN KEY (building_type) REFERENCES building_type(building_type),
    FOREIGN KEY (resource_type) REFERENCES resource_type(resource_type)
);
