CREATE TABLE ship (
    ship_id VARCHAR(40) NOT NULL,
    ship_type_id VARCHAR(40) NOT NULL,
    account_id VARCHAR(40) NOT NULL,
    planet_id VARCHAR(40) NOT NULL,
    fleet_id VARCHAR(40),
    ready TIMESTAMP,
    PRIMARY KEY (ship_id),
    FOREIGN KEY (ship_type_id) REFERENCES ship_type(ship_type_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id),
    FOREIGN KEY (planet_id) REFERENCES planet(planet_id),
    FOREIGN KEY (fleet_id) REFERENCES fleet(fleet_id)
);
