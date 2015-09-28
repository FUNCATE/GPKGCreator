create table if not exists TM_SETTINGS (ID integer primary key AUTOINCREMENT not null,KEY text,VALUE text);
CREATE TABLE IF NOT EXISTS TM_LAYER_SETTINGS (LAYER_NAME text primary key not null, ENABLED boolean not null,
POSITION integer not null unique, CONSTRAINT fk_layer_name FOREIGN KEY (LAYER_NAME) REFERENCES gpkg_contents(table_name));