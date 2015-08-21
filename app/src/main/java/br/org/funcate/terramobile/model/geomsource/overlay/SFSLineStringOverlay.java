package br.org.funcate.terramobile.model.geomsource.overlay;

import android.content.Context;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import br.org.funcate.terramobile.model.geomsource.SFSGeometry;

/**
 * Created by bogo on 21/08/15.
 */
public class SFSLineStringOverlay extends Polyline {

    private SFSLineStringOverlay(Context ctx) {
        super(ctx);
    }
    public SFSLineStringOverlay(Context ctx, SimpleFeature feature)
    {
        this(ctx);
        parseSFS(feature);
    }

    private void parseSFS(SimpleFeature feature)
    {
        if (feature != null)
        {
            if (feature.getDefaultGeometry() != null)
            {
                SimpleFeatureType type = feature.getType();
                Geometry geom = (Geometry) feature.getDefaultGeometry();

                //return parseSFS(geom);

                if ("LineString".equals(geom.getGeometryType())){
                    LineString l = (LineString) geom;
                    readLineString(l);
                }

            }
        }
    }

    private void readLineString(LineString l)
    {
        if(l!=null)
        {
            setPoints(parseSFSLineString(l));
        }
    }
    /*
    Tirar esse codigo daqui e colocar em algum serviço
     */
    private ArrayList<GeoPoint> parseSFSLineString(Geometry geometry)
    {
        if (geometry == null)
            return null;
        if(geometry instanceof LineString)
        {
            LineString l = (LineString) geometry;
            ArrayList<GeoPoint> coordinates = new  ArrayList<GeoPoint>(l.getCoordinates().length);
            for (int i = 0; i < l.getCoordinates().length; i++) {
                Coordinate c = l.getCoordinates()[i];
                GeoPoint p = new GeoPoint(c.y, c.x);
                coordinates.add(p);
            }
            return coordinates;
        }
        return null;
    }
}
