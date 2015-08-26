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
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.List;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.GeoUtil;

/**
 * Created by bogo on 20/08/15.
 */
public class SFSLayerOverlay extends Overlay {

    GpkgLayer layer=null;
    Context context=null;
    Style style=null;

    private SFSLayerOverlay(final Context ctx) {
        super(ctx);
        context = ctx;
    }

    public SFSLayerOverlay(GpkgLayer layer, final Context ctx)
    {
        this(ctx);
        this.layer=layer;
    }
    @Override
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
        System.out.println("======================§§§§§§§ USING SFSLayerOverlay = " + ((MainActivity) context).useNewOverlaySFS);
         if(!shadow)
        {
            try {
                BoundingBox bb = GeoUtil.convertToBoundingBox(osmv.getBoundingBox());
                List<SimpleFeature> features = new ArrayList<SimpleFeature>();
                if(!bb.contains(layer.getBox()))
                {
                    features = GeoPackageService.getGeometries(layer.getGeoPackage(), layer.getName(), bb);
                }
                else {
                    features = GeoPackageService.getGeometries(layer.getGeoPackage(), layer.getName(), null);
                }
                System.out.println("____----++++====[FEATURES QUERY, SIZE=" + features.size() + "]====++++----____");
                for (int i = 0; i < features.size(); i++) {
                    draw(features.get(i), c, osmv);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void draw(SimpleFeature feature, Canvas c, MapView osmv)
    {

        if (feature != null)
        {
            if (feature.getDefaultGeometry() != null)
            {
                SimpleFeatureType type = feature.getType();
                Geometry geom = (Geometry) feature.getDefaultGeometry();

                draw(geom, c, osmv);
            }
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
    public void setStyle(Style style)
    {
        this.style = style;
    }
}
