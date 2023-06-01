CREATE TABLE research_type (
    research_type VARCHAR(40) NOT NULL,
    characteristic VARCHAR(40) NOT NULL,
    research_in_seconds INT8 NOT NULL,
    building_in_seconds INT8 NOT NULL,
    level_base DECIMAL(8,6) NOT NULL,
    facility_base DECIMAL(8,6) NOT NULL,
    space_fix INT8 NOT NULL,
    space_per_level INT8 NOT NULL,
    benefit_basic_value INT8 NOT NULL,
    benefit_base DECIMAL(8,6) NOT NULL,
    benefit_exponent_modifier DECIMAL(8,6) NOT NULL,
    PRIMARY KEY (research_type),
    FOREIGN KEY (characteristic) REFERENCES characteristic(characteristic)
);
