CREATE TABLE research_type_research_prerequisite (
    research_type VARCHAR(40) NOT NULL,
    prerequisite VARCHAR(40) NOT NULL,
    level INT8 NOT NULL,
    PRIMARY KEY (research_type, prerequisite),
    FOREIGN KEY (research_type) REFERENCES research_type(research_type),
    FOREIGN KEY (prerequisite) REFERENCES research_type(research_type)
);
