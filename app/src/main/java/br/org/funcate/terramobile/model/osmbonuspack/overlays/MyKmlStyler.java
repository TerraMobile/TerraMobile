package br.org.funcate.terramobile.model.osmbonuspack.overlays;


import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

//13.2 Loading KML content - Advanced styling with Styler
public class MyKmlStyler implements KmlFeature.Styler {
    Style mDefaultStyle;
    KmlDocument kmlDocument;
    MapView map;

    public MyKmlStyler(Style defaultStyle, KmlDocument kmlDocument, MapView map){
        mDefaultStyle = defaultStyle;
        this.kmlDocument = kmlDocument;
        this.map=map;
    }

    @Override public void onLineString(Polyline polyline, KmlPlacemark kmlPlacemark, KmlLineString kmlLineString) {
        //Custom styling:
/*        polyline.setColor(Color.GREEN);
        polyline.setWidth(Math.max(kmlLineString.mCoordinates.size()/200.0f, 3.0f));*/
        kmlLineString.applyDefaultStyling(polyline, mDefaultStyle, kmlPlacemark, kmlDocument, map);
    }
    @Override public void onPolygon(Polygon polygon, KmlPlacemark kmlPlacemark, KmlPolygon kmlPolygon) {
        //Keeping default styling:
        kmlPolygon.applyDefaultStyling(polygon, mDefaultStyle, kmlPlacemark, kmlDocument, map);
    }
    @Override public void onPoint(Marker marker, KmlPlacemark kmlPlacemark, KmlPoint kmlPoint) {
        //Styling based on ExtendedData properties:
/*        if ("panda_area".equals(kmlPlacemark.getExtendedData("category")))
            kmlPlacemark.mStyle = "panda_area";
        else if ("gorilla_area".equals(kmlPlacemark.getExtendedData("category")))
            kmlPlacemark.mStyle = "gorilla_area";*/
        kmlPoint.applyDefaultStyling(marker, mDefaultStyle, kmlPlacemark, kmlDocument, map);
    }
    @Override public void onFeature(Overlay overlay, KmlFeature kmlFeature) {
        //If nothing to do, do nothing.
    }
}