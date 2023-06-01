CREATE TABLE research_level (
    research_type VARCHAR(40) NOT NULL,
    account_id VARCHAR(40) NOT NULL,
    level INT8 NOT NULL,
    next_level_update TIMESTAMP,
    planet_id VARCHAR(40),
    PRIMARY KEY (research_type, account_id),
    FOREIGN KEY (research_type) REFERENCES research_type(research_type),
    FOREIGN KEY (account_id) REFERENCES account(account_id),
    FOREIGN KEY (planet_id) REFERENCES planet(planet_id)
);
