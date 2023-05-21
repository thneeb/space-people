CREATE TABLE ship_type (
    ship_type_id VARCHAR(40) NOT NULL,
    account_id VARCHAR(40) NOT NULL,
    nickname VARCHAR(80) NOT NULL,
    stability INT8 NOT NULL,
    armour INT8 NOT NULL,
    attack INT8 NOT NULL ,
    fuel_units INT8 NOT NULL,
    cargo_units INT8 NOT NULL,
    acceleration INT8 NOT NULL,
    hydrogen_consumption_per_hour INT8 NOT NULL,
    PRIMARY KEY (ship_type_id),
    UNIQUE (account_id, nickname),
    FOREIGN KEY (account_id) REFERENCES account(account_id)
);
