CREATE TABLE fleet (
    fleet_id VARCHAR(40) NOT NULL,
    nickname VARCHAR(40) NOT NULL,
    account_id VARCHAR(40) NOT NULL,
    fleet_status VARCHAR(40) NOT NULL,
    planet_id VARCHAR(40) NOT NULL,
    next_status_update TIMESTAMP,
    PRIMARY KEY (fleet_id),
    UNIQUE (nickname, account_id),
    FOREIGN KEY (account_id) REFERENCES account(account_id),
    FOREIGN KEY (fleet_status) REFERENCES fleet_status(fleet_status),
    FOREIGN KEY (planet_id) REFERENCES planet(planet_id)
);
