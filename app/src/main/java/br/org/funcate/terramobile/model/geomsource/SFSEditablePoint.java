package br.org.funcate.terramobile.model.geomsource;

import com.vividsolutions.jts.geom.Point;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import br.org.funcate.terramobile.model.osmbonuspack.overlays.SFSEditableMarker;

/**
 * Created by Andre Carvalho on 23/09/15.
 */
public class SFSEditablePoint extends SFSPoint {

    public SFSEditablePoint(Point point){
        super(point);
    }

    public Overlay buildOverlay(MapView map, Style defaultStyle, KmlFeature.Styler styler, KmlPlacemark kmlPlacemark, KmlDocument kmlDocument) {
        Marker marker = new SFSEditableMarker(map);
        marker.setTitle(kmlPlacemark.mName);
        marker.setSnippet(kmlPlacemark.mDescription);
        marker.setSubDescription(kmlPlacemark.getExtendedDataAsText());
        marker.setPosition(this.getPosition());
        this.mId=kmlPlacemark.mId;
        marker.setRelatedObject(this);
        if(styler == null) {
            this.applyDefaultStyling(marker, defaultStyle, kmlPlacemark, kmlDocument, map);
        } else {
            styler.onPoint(marker, kmlPlacemark, this);
        }

        return marker;
    }
}
