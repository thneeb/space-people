CREATE TABLE ship_type_equipment (
    ship_type_id   VARCHAR(40) NOT NULL,
    ship_part_type VARCHAR(40) NOT NULL,
    level          INT8,
    PRIMARY KEY (ship_type_id, ship_part_type),
    FOREIGN KEY (ship_type_id) REFERENCES ship_type (ship_type_id),
    FOREIGN KEY (ship_part_type) REFERENCES ship_part_type(ship_part_type)
);
