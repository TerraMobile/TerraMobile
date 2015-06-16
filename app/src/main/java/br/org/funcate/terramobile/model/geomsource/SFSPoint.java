package br.org.funcate.terramobile.model.geomsource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osmdroid.bonuspack.kml.KmlGeometry;
import org.osmdroid.bonuspack.kml.KmlPoint;

/**
 * Created by bogo on 15/06/15.
 */
public class SFSPoint extends KmlPoint {

    public SFSPoint(Point point){
        super();

        if(point!=null)
        {
            setPosition(SFSGeometry.parseSFSPoint(point));
        }
    }
}
