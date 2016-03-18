package br.org.funcate.terramobile.model.geomsource;

import com.google.gson.JsonObject;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlGeometry;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.io.Writer;
import java.util.ArrayList;

import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

/**
 * Created by bogo on 15/06/15.
 */
public class SFSGeometry extends KmlGeometry {

    @Override
    public void saveAsKML(Writer writer) {

    }

    @Override
    public JsonObject asGeoJSON() {
        return null;
    }

    @Override
    public Overlay buildOverlay(MapView map, Style defaultStyle, KmlFeature.Styler styler, KmlPlacemark kmlPlacemark, KmlDocument kmlDocument) {
        return null;
    }

    @Override
    public BoundingBoxE6 getBoundingBox() {
        return null;
    }

    public static KmlGeometry parseSFS(SimpleFeature feature, GpkgLayer layer){
        if (feature != null)
        {
            if (feature.getDefaultGeometry() != null)
            {
                SimpleFeatureType type = feature.getType();
                Geometry geom = (Geometry) feature.getDefaultGeometry();

                return parseSFS(geom, layer);

            }
        }
        return null;


    }

    public static KmlGeometry parseSFS(Geometry geom, GpkgLayer layer){

        if ("Point".equals(geom.getGeometryType())){
            Point p = (Point) geom;
            if(GpkgLayer.Type.EDITABLE == layer.getType())
                return new SFSEditablePoint(p);
            else
                return new SFSPoint(p);
        } else if ("LineString".equals(geom.getGeometryType())){
            LineString l = (LineString) geom;
            return new SFSLineString(l);
        } else if ("Polygon".equals(geom.getGeometryType())){
            Polygon p = (Polygon) geom;
            return new SFSPolygon(p);
        } else if ("MultiPoint".equals(geom.getGeometryType()) || "MultiLineString".equals(geom.getGeometryType()) || "MultiPolygon".equals(geom.getGeometryType())){
            return new SFSMultiGeometry(geom, layer);
        } else
            return null;
    }

    public static GeoPoint parseSFSPoint(Geometry geometry)
    {
        if(geometry instanceof Point)
        {
            Point p = (Point) geometry;
            return new GeoPoint(p.getY(),p.getX(),.0);
        }
        return null;

    }

    public static ArrayList<GeoPoint> parseSFSLineString(Geometry geometry)
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
