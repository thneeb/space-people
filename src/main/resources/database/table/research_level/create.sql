CREATE TABLE research_level (
    ship_part_type VARCHAR(40) NOT NULL,
    account_id VARCHAR(40) NOT NULL,
    level INT8 NOT NULL,
    next_level_update TIMESTAMP,
    planet_id VARCHAR(40),
    PRIMARY KEY (ship_part_type, account_id),
    FOREIGN KEY (ship_part_type) REFERENCES ship_part_type(ship_part_type),
    FOREIGN KEY (account_id) REFERENCES account(account_id),
    FOREIGN KEY (planet_id) REFERENCES planet(planet_id)
);
