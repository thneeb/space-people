CREATE TABLE building_type (
    building_type VARCHAR(40) NOT NULL,
    name VARCHAR(40) NOT NULL,
    duration_in_seconds INT8 NOT NULL,
    duration_basis DECIMAL(8,6) NOT NULL,
    PRIMARY KEY (building_type)
);
