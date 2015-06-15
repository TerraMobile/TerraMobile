package br.org.funcate.terramobile.model.geomsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import org.opengis.feature.simple.SimpleFeature;
import org.osmdroid.bonuspack.kml.KmlGeometry;
import org.osmdroid.bonuspack.kml.KmlLineString;

/**
 * Created by bogo on 15/06/15.
 */
public class SFSLineString extends KmlLineString {

    public SFSLineString(LineString line){
        super();
        if(line!=null)
        {
            mCoordinates = SFSGeometry.parseSFSLineString(line);
        }

    }
}
