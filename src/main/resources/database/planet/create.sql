CREATE TABLE planet (
    planet_id VARCHAR(40) NOT NULL,
    galaxy_id VARCHAR(40) NOT NULL,
    coordinate_x INT2 NOT NULL,
    coordinate_y INT2 NOT NULL,
    coordinate_z INT2 NOT NULL,
    orbit CHAR(1) NOT NULL,
    name VARCHAR(80) NOT NULL,
    account_id VARCHAR(40),
    PRIMARY KEY (planet_id),
    UNIQUE (galaxy_id, coordinate_x, coordinate_y, coordinate_z, orbit),
    FOREIGN KEY (galaxy_id) REFERENCES galaxy (galaxy_id)
);
