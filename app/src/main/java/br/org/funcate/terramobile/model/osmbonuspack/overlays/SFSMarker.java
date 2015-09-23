package br.org.funcate.terramobile.model.osmbonuspack.overlays;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.osmdroid.bonuspack.overlays.BasicInfoWindow;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.geomsource.SFSPoint;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;

/**
 * Created by Andre Carvalho on 31/07/15.
 */
public class SFSMarker extends Marker implements Marker.OnMarkerClickListener {

    public SFSMarker(MapView mapView) {
        super(mapView);
        this.setOnMarkerClickListener(this);
        this.setInfoWindow(new TMMarkerInfoWindow(R.layout.marker_info_window, mapView));
        this.setTitle("Centered on " + this.getPosition().getLatitude() + "," + this.getPosition().getLongitude());
    }

    public boolean onMarkerClick(Marker marker, MapView mapView) {
        marker.showInfoWindow();
        return true;
    }

    public String getFeatureId() {
        return ((SFSPoint)this.getRelatedObject()).mId;
    }

    private class TMMarkerInfoWindow extends BasicInfoWindow {

        private SFSMarker mMarker = null;

        public TMMarkerInfoWindow(int layoutResId, MapView mapView) {
            super(layoutResId, mapView);
        }

        public void onOpen(Object arg0) {
            closeAllInfoWindowsOn(this.mMapView);
            if(arg0 instanceof SFSMarker) {
                mMarker = (SFSMarker) arg0;
            }
            TextView txtTitle = (TextView) mView.findViewById(R.id.bubble_title);
            String titleMarker = "";
            try {
                titleMarker = ResourceHelper.getStringResource(R.string.title_marker);

            } catch (InvalidAppConfigException e) {
                Message.showErrorMessage(((MainActivity) mMapView.getContext()), R.string.failure_title_msg, e.getMessage());
            }
            titleMarker += ": " + mMarker.getPosition().toDoubleString();
            txtTitle.setText(titleMarker);

            ImageButton btnCloseInfo = (ImageButton) mView.findViewById(R.id.btn_close_info_window);
            btnCloseInfo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mMarker.closeInfoWindow();
                }
            });
        }
    }

}
