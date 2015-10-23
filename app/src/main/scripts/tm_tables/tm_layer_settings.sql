DROP TABLE TM_LAYER_SETTINGS;
CREATE TABLE TM_LAYER_SETTINGS (LAYER_NAME text primary key not null, ENABLED boolean not null,
POSITION integer not null unique, CONSTRAINT fk_layer_name FOREIGN KEY (LAYER_NAME) REFERENCES gpkg_contents(table_name));