CREATE TABLE building_capacity_usage (
    building_type VARCHAR(40) NOT NULL,
    capacity_type VARCHAR(40) NOT NULL,
    basic_value INT8 NOT NULL,
    base DECIMAL(8,6) NOT NULL,
    exponent_modifier DECIMAL(8,6) NOT NULL,
    PRIMARY KEY (building_type, capacity_type),
    FOREIGN KEY (building_type) REFERENCES building_type(building_type),
    FOREIGN KEY (capacity_type) REFERENCES capacity_type(capacity_type)
);
