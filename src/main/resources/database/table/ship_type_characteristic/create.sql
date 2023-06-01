CREATE TABLE ship_type_characteristic (
    ship_type_id VARCHAR(40) NOT NULL,
    characteristic VARCHAR(40) NOT NULL,
    value int8 NOT NULL,
    PRIMARY KEY (ship_type_id, characteristic),
    FOREIGN KEY (ship_type_id) REFERENCES ship_type(ship_type_id),
    FOREIGN KEY (characteristic) REFERENCES characteristic(characteristic)
);
