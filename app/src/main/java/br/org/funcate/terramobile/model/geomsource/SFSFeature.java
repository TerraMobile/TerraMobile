package br.org.funcate.terramobile.model.geomsource;

import com.google.gson.JsonObject;

import org.opengis.feature.simple.SimpleFeature;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.io.Writer;

/**
 * Created by bogo on 15/06/15.
 */
public class SFSFeature extends KmlFeature {

    private SFSFeature()
    {
        super();
    }

    @Override
    public BoundingBoxE6 getBoundingBox() {
        return null;
    }

    @Override
    public Overlay buildOverlay(MapView map, Style defaultStyle, Styler styler, KmlDocument kmlDocument) {
        return null;
    }

    @Override
    public void writeKMLSpecifics(Writer writer) {

    }

    @Override
    public JsonObject asGeoJSON(boolean isRoot) {
        return null;
    }

    public static KmlFeature parseSFS(SimpleFeature sfsFeature){
        if (sfsFeature == null)
            return null;
        return new SFSPlacemark(sfsFeature);
   }
}
