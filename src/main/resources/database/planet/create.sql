CREATE TABLE planet (
    planet_id VARCHAR(40) NOT NULL,
    coordinate_x INT2 NOT NULL,
    coordinate_y INT2 NOT NULL,
    coordinate_z INT2 NOT NULL,
    planet_letter CHAR(1) NOT NULL,
    name VARCHAR(80) NOT NULL,
    account_id VARCHAR(40),
    sun INT2 NOT NULL,
    water INT2 NOT NULL,
    silicon INT2 NOT NULL,
    iron INT2 NOT NULL,
    carbon INT2 NOT NULL,
    PRIMARY KEY (planet_id)
);
