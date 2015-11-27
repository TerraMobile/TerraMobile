package br.org.funcate.terramobile.model.geomsource.overlay;

import android.content.Context;
import android.graphics.Canvas;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.GeoUtil;

/**
 * Created by bogo on 16/10/15.
 */
public class SFSOverlayThread implements Runnable {

    GpkgLayer layer=null;
    Context context=null;
    Style style=null;

    Canvas c=null;
    MapView osmv=null;

    public SFSOverlayThread(GpkgLayer layer, Context context, Style style, Canvas c, MapView osmv)
    {
        this.layer = layer;
        this.context = context;
        this.style = style;
        this.c=c;
        this.osmv=osmv;
    }
    @Override
    public void run() {


        try {


            BoundingBox bb = GeoUtil.convertToBoundingBox(osmv.getBoundingBox());
            List<SimpleFeature> features = new ArrayList<SimpleFeature>();


            if(bb.intersects(layer.getBox()))
            {
                System.out.println("____----++++====[INTERSECTS]====++++----____");
            }
            else
            {
                System.out.println("____----++++====[NO INTERSECTION]====++++----____");
                return;
            }

            features = GeoPackageService.getGeometries(layer.getGeoPackage(), layer.getName(), bb);

            System.out.println("____----++++====[FEATURES QUERY, SIZE=" + features.size() + "]====++++----____");
            for (int i = 0; i < features.size(); i++) {
                //draw(features.get(i), c, osmv);

                if (features.get(i) != null)
                {
                    if (features.get(i).getDefaultGeometry() != null)
                    {
                        SimpleFeatureType type = features.get(i).getType();
                        Geometry geom = (Geometry) features.get(i).getDefaultGeometry();

                        draw(geom, c, osmv);
                    }
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void draw(Geometry geom, Canvas c, MapView osmv)
    {
        if ("Point".equals(geom.getGeometryType())){
            Point p = (Point) geom;
            //return new SFSPoint(p);
        } else if ("LineString".equals(geom.getGeometryType())){
            LineString l = (LineString) geom;
            SFSLineStringOverlay overlay = new SFSLineStringOverlay(context, l);
            if(style!=null)
            {
                overlay.setStyle(style);
            }
            overlay.draw(c, osmv, false);
        } else if ("Polygon".equals(geom.getGeometryType())){
            Polygon p = (Polygon) geom;
            SFSPolygonOverlay overlay = new SFSPolygonOverlay(context, p);
            if(style!=null)
            {
                overlay.setStyle(style);
            }
            overlay.draw(c, osmv, false);
        } else if ("MultiPoint".equals(geom.getGeometryType()) || "MultiLineString".equals(geom.getGeometryType()) || "MultiPolygon".equals(geom.getGeometryType())){
            drawMulti(geom, c, osmv);
        }
    }
    private void drawMulti(Geometry geometry, Canvas c, MapView osmv)
    {
        if(geometry!=null) {

            GeometryCollection collection=  (GeometryCollection) geometry;
            for (int i = 0; i < collection.getNumGeometries(); i++)
            {
                Geometry geom = collection.getGeometryN(i);
                draw(geom, c, osmv);
            }
        }
    }
}
