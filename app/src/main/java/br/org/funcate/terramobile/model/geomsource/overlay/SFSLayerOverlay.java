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
import java.util.HashMap;
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

         if(!shadow)
        {
            new Thread(new SFSOverlayThread(layer, context, style, c, osmv)).start();
        }
    }


    public void setStyle(Style style)
    {
        this.style = style;
    }
}
