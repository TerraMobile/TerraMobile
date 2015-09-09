DROP TABLE tm_layer_form;

CREATE TABLE IF NOT EXISTS tm_layer_form (
tm_conf_id INTEGER PRIMARY KEY AUTOINCREMENT,
gpkg_layer_identify TEXT NOT NULL,
tm_form TEXT,
tm_media_table TEXT,
CONSTRAINT fk_layer_identify_id FOREIGN KEY (gpkg_layer_identify) REFERENCES gpkg_contents(table_name)
);

DROP TABLE "field_work_collect_data";
-- Describe FIELD_WORK_COLLECT_DATA
CREATE TABLE IF NOT EXISTS "field_work_collect_data" (
"field_work_id" INTEGER PRIMARY KEY AUTOINCREMENT,
"location_name" TEXT,
"temperature" DOUBLE,
"soil_ph" INTEGER,
"collect_date" DATETIME,
"termites" BOOLEAN,
"crop_stage" TEXT,
"geometry" BLOB);

DROP TABLE "field_work_picture_data";
-- Describe FIELD_WORK_PICTURE_DATA
CREATE TABLE IF NOT EXISTS "field_work_picture_data" (
"PK_UID" INTEGER PRIMARY KEY AUTOINCREMENT,
"feature_id" INTEGER NOT NULL,
"picture" BLOB,
"picture_mime_type" TEXT,
CONSTRAINT fk_feature_id FOREIGN KEY (feature_id) REFERENCES field_work_collect_data(field_work_id) ON DELETE CASCADE
);

DELETE FROM gpkg_geometry_columns WHERE table_name = 'field_work_collect_data';

INSERT INTO gpkg_geometry_columns ("geometry_type_name", "m", "z", "srs_id", "column_name", "table_name") values('POINT', '0', '0', '4326', 'geometry', 'field_work_collect_data');

DELETE FROM gpkg_contents WHERE table_name = 'field_work_collect_data';

INSERT INTO gpkg_contents ("table_name", "data_type", "identifier", "description", "last_change", "min_x", "min_y", "max_x", "max_y", "srs_id") values('field_work_collect_data', 'features', 'field_work_collect_data', 'field_work_collect_data', strftime('%Y-%m-%dT%H:%M:%fZ','now','localtime'), -51.544052, -20.910128, -50.96211, -20.7295, 4326);

DELETE FROM tm_layer_form WHERE gpkg_layer_identify = 'field_work_collect_data';

INSERT INTO tm_layer_form ("gpkg_layer_identify", "tm_form", "tm_media_table" )
values('field_work_collect_data','[{
        "sectionname": "terramobile",
        "sectiondescription": "Data collector for field work",
        "forms": [
            {
                "formname": "field_work_collect_data",
                "formitems": [
                    {
                        "key": "location_name",
                        "label": "The name of this location",
                        "value": "",
                        "type": "string"
                    },{
                        "key": "temperature",
                        "label": "The temperature in this location",
                        "value": "",
                        "type": "double"
                    },{
                        "key": "soil_ph",
                        "label": "The number of the soil pH",
                        "value": "",
                        "type": "integer"
                    },{
                        "key": "collect_date",
                        "label": "The date of the gathering data",
                        "value": "",
                        "type": "date"
                    },{
                        "key": "termites",
                        "label": "Presence of termites?",
                        "value": "",
                        "type": "boolean"
                    },{
                        "key": "crop_stage",
                        "label": "Crop development stage",
                        "values": {
                            "items": [
                                {"item": ""},
                                {"item": "stage 1"},
                                {"item": "stage 2"},
                                {"item": "stage 3"},
                                {"item": "stage 4"},
                                {"item": "stage 5"}
                            ]
                        },
                        "value": "",
                        "type": "stringcombo"
                    },{
                        "key": "picture",
                        "label": "Take photos",
                        "value": "",
                        "type": "pictures"
                    }
                ]
            }
        ]
    }
]', 'field_work_picture_data');