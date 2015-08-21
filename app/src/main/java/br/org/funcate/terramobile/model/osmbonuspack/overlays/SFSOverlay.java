package br.org.funcate.terramobile.model.osmbonuspack.overlays;

import android.content.Context;
import android.graphics.Canvas;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.List;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.GeoUtil;

/**
 * Created by bogo on 20/08/15.
 */
public class SFSOverlay extends Overlay {

    GpkgLayer layer=null;

    private SFSOverlay(final Context ctx) {
        super(ctx);
    }

    public SFSOverlay(GpkgLayer layer, final Context ctx)
    {
        this(ctx);
        this.layer=layer;
    }
    @Override
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
        if(!shadow)
        {
            try {
                BoundingBox bb = GeoUtil.convertToBoundingBox(osmv.getBoundingBox());
                if(!bb.contains(layer.getBox()))
                {
                    List<SimpleFeature> features = GeoPackageService.getGeometries(layer.getGeoPackage(), layer.getName(), bb);
                    System.out.println("Getting BBOX FEATURES: "+features.size());
                }
                else {
                    List<SimpleFeature> features = GeoPackageService.getGeometries(layer.getGeoPackage(), layer.getName(), null);
                    System.out.println("Getting ALL FEATURES: "+features.size());
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
