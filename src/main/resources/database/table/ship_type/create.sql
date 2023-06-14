CREATE TABLE ship_type (
    ship_type_id VARCHAR(40) NOT NULL,
    account_id VARCHAR(40) NOT NULL,
    nickname VARCHAR(80) NOT NULL,
    manned INT2 NOT NULL,
    planet_id VARCHAR(40),
    ready TIMESTAMP,
    PRIMARY KEY (ship_type_id),
    UNIQUE (account_id, nickname),
    FOREIGN KEY (account_id) REFERENCES account(account_id),
    FOREIGN KEY (planet_id) REFERENCES planet(planet_id)
);
