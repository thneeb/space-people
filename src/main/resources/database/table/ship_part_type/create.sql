CREATE TABLE ship_part_type (
    ship_part_type VARCHAR(40) NOT NULL,
    duration_in_seconds INT8 NOT NULL,
    duration_basis DECIMAL(8,6) NOT NULL,
    space_fix INT8 NOT NULL,
    space_per_level INT8 NOT NULL,
    PRIMARY KEY (ship_part_type)
);
