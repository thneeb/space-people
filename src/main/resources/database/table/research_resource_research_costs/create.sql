CREATE TABLE research_resource_research_costs (
    research_type VARCHAR(40) NOT NULL,
    resource_type VARCHAR(40) NOT NULL,
    basic_value INT8 NOT NULL,
    base DECIMAL(8,6) NOT NULL,
    exponent_modifier DECIMAL(8,6) NOT NULL,
    PRIMARY KEY (research_type, resource_type),
    FOREIGN KEY (research_type) REFERENCES research_type(research_type),
    FOREIGN KEY (resource_type) REFERENCES resource_type(resource_type)
);
