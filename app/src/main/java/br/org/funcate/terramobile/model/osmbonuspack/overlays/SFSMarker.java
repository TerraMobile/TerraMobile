package br.org.funcate.terramobile.model.osmbonuspack.overlays;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.BasicInfoWindow;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.AbstractList;

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
        //marker.closeInfoWindow();
        return true;
    }

    private class TMMarkerInfoWindow extends BasicInfoWindow {

        private String markerId;
        private SFSMarker m = null;

        public TMMarkerInfoWindow(int layoutResId, MapView mapView) {
            super(layoutResId, mapView);
        }

        public void onClose() {
        }

        public void onOpen(Object arg0) {
            closeAllInfoWindowsOn(this.mMapView);
            if(arg0 instanceof SFSMarker) {
                m = (SFSMarker) arg0;
                markerId = ((SFSPoint)m.getRelatedObject()).mId;
            }
            //LinearLayout layout = (LinearLayout) mView.findViewById(R.id.bubble_layout);
            TextView txtTitle = (TextView) mView.findViewById(R.id.bubble_title);
            /*TextView txtDescription = (TextView) mView.findViewById(R.id.bubble_description);
            TextView txtSubdescription = (TextView) mView.findViewById(R.id.bubble_subdescription);*/
            String titleMarker = "";
            try {
                titleMarker = ResourceHelper.getStringResource(R.string.title_marker);

            } catch (InvalidAppConfigException e) {
                Message.showErrorMessage(((MainActivity) mMapView.getContext()), R.string.failure_title_msg, e.getMessage());
            }
            titleMarker += ": " + m.getPosition().toDoubleString();
            txtTitle.setText(titleMarker);
            /*txtDescription.setText("Location:" + m.getPosition().toDoubleString());
            txtSubdescription.setText("You can also edit the subdescription");
            txtSubdescription.setVisibility(txtSubdescription.INVISIBLE);*/

            ImageButton btnMoreInfo = (ImageButton) mView.findViewById(R.id.bubble_moreinfo);
            btnMoreInfo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Implement onClick behaviour
                }
            });
            ImageButton btnEdit = (ImageButton) mView.findViewById(R.id.btn_edit_marker);
            btnEdit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Implement onClick behaviour
                }
            });
            ImageButton btnRemove = (ImageButton) mView.findViewById(R.id.btn_remove_marker);
            btnRemove.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    FolderOverlay f = getRelatedFolder();
                    //assert f != null;
                    if (f!=null && f.remove(m)) {
                        m.closeInfoWindow();
                        mMapView.invalidate();
                    } else {
                        //Fail on remove this point
                        Message.showErrorMessage(
                                (MainActivity)mMapView.getContext(),
                                R.string.title_fail_message_marker,
                                R.string.fail_message_on_remove_marker
                        );
                    }
                    return;
                }
            });
            ImageButton btnGPS = (ImageButton) mView.findViewById(R.id.btn_move_to_gps);
            btnGPS.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Implement onClick behaviour
                }
            });
            ImageButton btnMapCenter = (ImageButton) mView.findViewById(R.id.btn_move_to_map_center);
            btnMapCenter.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    IGeoPoint iPoint = mMapView.getMapCenter();
                    GeoPoint location=new GeoPoint(iPoint.getLatitudeE6(),iPoint.getLongitudeE6());
                    m.setPosition(location);
                    m.closeInfoWindow();
                    mMapView.invalidate();
                    m.showInfoWindow();
                }
            });
            ImageButton btnCloseInfo = (ImageButton) mView.findViewById(R.id.btn_close_info_window);
            btnCloseInfo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    m.closeInfoWindow();
                }
            });
        }

        private FolderOverlay getRelatedFolder() {
            for (int j = 0, len = mMapView.getOverlayManager().size(); j < len; j++) {
                Overlay overlay = mMapView.getOverlayManager().get(j);
                if (overlay instanceof FolderOverlay) {
                    FolderOverlay f = (FolderOverlay) overlay;
                    AbstractList<Overlay> listOverlay = f.getItems();
                    int index = listOverlay.indexOf(m);
                    if(index>=0) return f;
                }
            }
            return null;
        }
    }
}
