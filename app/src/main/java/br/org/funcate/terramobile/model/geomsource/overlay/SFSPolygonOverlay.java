package br.org.funcate.terramobile.model.geomsource.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.overlays.OverlayWithIW;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

import br.org.funcate.terramobile.model.geomsource.SFSGeometry;

/**
 * Created by bogo on 21/08/15.
 */
public class SFSPolygonOverlay extends Polygon {

    private SFSPolygonOverlay(Context ctx) {
        super(ctx);
    }
    public SFSPolygonOverlay(Context ctx, Geometry geom)
    {
        this(ctx);
        parseSFS(geom);
    }

    private void parseSFS(Geometry geom)
    {
        com.vividsolutions.jts.geom.Polygon p = (com.vividsolutions.jts.geom.Polygon) geom;
        readPolygon(p);
    }
    private void readPolygon(com.vividsolutions.jts.geom.Polygon p)
    {
        if(p!=null)
        {
            setPoints(parseSFSLineString(p.getExteriorRing()));

            if(p.getNumInteriorRing()>0)
            {
                setHoles(new ArrayList<ArrayList<GeoPoint>>(p.getNumInteriorRing()));
                for (int i=1; i<p.getNumInteriorRing(); i++)
                {
                    ArrayList<GeoPoint> pList = parseSFSLineString(p.getInteriorRingN(i));
                    getHoles().add(pList);
                }
            }
        }
    }

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
    public void draw(final Canvas canvas, final MapView mapView, final boolean shadow)
    {
        super.draw(canvas, mapView, shadow);
    }

    public void setStyle(Style defaultStyle)
    {
        Paint outlinePaint = defaultStyle.getOutlinePaint();
        setStrokeColor(outlinePaint.getColor());
        setStrokeWidth(outlinePaint.getStrokeWidth());
        int fillColor = defaultStyle.mPolyStyle.getFinalColor();
        setFillColor(fillColor);
    }
}
