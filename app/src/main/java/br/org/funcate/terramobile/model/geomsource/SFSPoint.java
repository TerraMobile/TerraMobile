package br.org.funcate.terramobile.model.geomsource;

import android.content.Context;

import com.vividsolutions.jts.geom.Point;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import br.org.funcate.terramobile.model.osmbonuspack.overlays.SFSMarker;

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

    @Override public void applyDefaultStyling(Marker marker, Style defaultStyle, KmlPlacemark kmlPlacemark,
                                    KmlDocument kmlDocument, MapView map){
        Context context = map.getContext();
        Style style = kmlDocument.getStyle(kmlPlacemark.mStyle);
        if (style != null && style.mIconStyle != null){
            style.mIconStyle.styleMarker(marker, context);
        } else if (defaultStyle!=null && defaultStyle.mIconStyle!=null){
            defaultStyle.mIconStyle.styleMarker(marker, context);
        }
        //allow marker drag, acting on KML Point:
        marker.setDraggable(false);
        marker.setOnMarkerDragListener(new OnKMLMarkerDragListener());
        marker.setEnabled(kmlPlacemark.mVisibility);
    }

    public Overlay buildOverlay(MapView map, Style defaultStyle, KmlFeature.Styler styler, KmlPlacemark kmlPlacemark, KmlDocument kmlDocument) {
        Marker marker = new SFSMarker(map);
        marker.setTitle(kmlPlacemark.mName);
        marker.setSnippet(kmlPlacemark.mDescription);
        marker.setSubDescription(kmlPlacemark.getExtendedDataAsText());
        marker.setPosition(this.getPosition());
        marker.setRelatedObject(this);
        if(styler == null) {
            this.applyDefaultStyling(marker, defaultStyle, kmlPlacemark, kmlDocument, map);
        } else {
            styler.onPoint(marker, kmlPlacemark, this);
        }

        return marker;
    }
}
