CREATE TABLE galaxy (
    galaxy_id VARCHAR(40) NOT NULL,
    nickname VARCHAR(80) NOT NULL,
    PRIMARY KEY (galaxy_id),
    UNIQUE (nickname)
);