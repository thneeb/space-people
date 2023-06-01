CREATE TABLE ship_type_equipment (
    ship_type_id   VARCHAR(40) NOT NULL,
    research_type VARCHAR(40) NOT NULL,
    level          INT8,
    PRIMARY KEY (ship_type_id, research_type),
    FOREIGN KEY (ship_type_id) REFERENCES ship_type (ship_type_id),
    FOREIGN KEY (research_type) REFERENCES research_type(research_type)
);
