package br.org.funcate.terramobile.model.osmbonuspack.overlays;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.augtech.geoapi.geopackage.table.GpkgExtensions;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.overlays.BasicInfoWindow;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.AbstractList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.controller.activity.MainController;
import br.org.funcate.terramobile.controller.activity.MarkerInfoWindowController;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.geomsource.SFSPoint;
import br.org.funcate.terramobile.model.service.GPSService;
import br.org.funcate.terramobile.util.CallbackConfirmMessage;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;

/**
 * Created by Andre Carvalho on 31/07/15.
 */
public class SFSMarker extends Marker implements Marker.OnMarkerClickListener {

    private MainActivity mMainActivity=null;

    public SFSMarker(MapView mapView) {
        super(mapView);
        this.setOnMarkerClickListener(this);
        this.setInfoWindow(new TMMarkerInfoWindow(R.layout.marker_info_window,
                mapView,
                ((MainActivity) mapView.getContext()).getMarkerInfoWindowController()
        ));
        this.setTitle("Centered on " + this.getPosition().getLatitude() + "," + this.getPosition().getLongitude());
        this.mMainActivity = (MainActivity) mapView.getContext();
    }

    public boolean onMarkerClick(Marker marker, MapView mapView) {
        marker.showInfoWindow();
        //marker.closeInfoWindow();
        return true;
    }

    public Long getMarkerId() {
        String markerId = ((SFSPoint)this.getRelatedObject()).mId;
        String editableLayerName = this.mMainActivity.getMainController().getTreeViewController().getSelectedEditableLayer().getName();
        markerId = markerId.replaceFirst(editableLayerName,"");
        return new Long(markerId);
    }

    public String getFeatureId() {
        return ((SFSPoint)this.getRelatedObject()).mId;
    }

    private class TMMarkerInfoWindow extends BasicInfoWindow implements CallbackConfirmMessage {

        private final int MOVE_TO_GPS = 0;
        private final int MOVE_TO_CENTER = 1;
        private final int REMOVE_MAKER = 2;


        private SFSMarker m = null;
        private MarkerInfoWindowController markerInfoWindowController = null;
        private TMMarkerInfoWindow that=null;
        private int who;

        public TMMarkerInfoWindow(int layoutResId, MapView mapView, MarkerInfoWindowController markerInfoWindowController) {
            super(layoutResId, mapView);
            this.markerInfoWindowController=markerInfoWindowController;
            that=this;
        }

        public void confirmResponse(boolean response){
            if(response) {
                switch (this.who){
                    case MOVE_TO_GPS:
                        moveToGPS();
                        break;
                    case MOVE_TO_CENTER:
                        moveToCenter();
                        break;
                    case REMOVE_MAKER:
                        remove();
                        break;
                }
            }
        }
        public void setWhoCall(int whoCall){
            this.who=whoCall;
        }

        private void moveToGPS() {
            String waitingMsg="";
            try {
                waitingMsg = ResourceHelper.getStringResource(R.string.message_waiting_location_gps);
            }catch (InvalidAppConfigException e) {
                e.printStackTrace();
                waitingMsg="Waiting...";
            }

            final ProgressDialog progressDialog = Message.startProgressDialog(mMapView.getContext(), waitingMsg);
            // Define a listener that responds to location updates
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    GPSService.unregisterListener(mMapView.getContext(), this);
                    // Called when a new location is found by the GPS location provider.
                    GeoPoint newPoint=new GeoPoint(location);
                    GeoPoint oldPoint=m.getPosition();
                    try {
                        m.setPosition(newPoint);
                        markerInfoWindowController.moveMarker(m);
                    }catch (TerraMobileException tme){
                        tme.printStackTrace();
                        Message.showErrorMessage((MainActivity) mMapView.getContext(), R.string.fail, tme.getMessage());
                        m.setPosition(oldPoint);
                        progressDialog.dismiss();
                    }
                    m.closeInfoWindow();
                    mMapView.invalidate();
                    m.showInfoWindow();
                    progressDialog.dismiss();
                }
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                public void onProviderEnabled(String provider) {}
                public void onProviderDisabled(String provider) {}
            };

            if(!GPSService.registerListener(mMapView.getContext(), locationListener)) {
                Message.showErrorMessage((MainActivity)mMapView.getContext(),R.string.fail,R.string.disabled_provider);
            }
        }

        private void moveToCenter() {
            IGeoPoint iPoint = mMapView.getMapCenter();
            GeoPoint newPoint=new GeoPoint(iPoint.getLatitude(),iPoint.getLongitude());
            GeoPoint oldPoint=m.getPosition();
            try {
                m.setPosition(newPoint);
                markerInfoWindowController.moveMarker(m);
            }catch (TerraMobileException tme){
                tme.printStackTrace();
                Message.showErrorMessage((MainActivity) mMapView.getContext(), R.string.fail, tme.getMessage());
                m.setPosition(oldPoint);
            }
            m.closeInfoWindow();
            mMapView.invalidate();
            m.showInfoWindow();
        }

        private void remove() {
            try {
                markerInfoWindowController.deleteMarker(m);
            } catch (TerraMobileException e) {
                e.printStackTrace();
                showErrorMessage(R.string.failure_on_remove_marker);
            }

            FolderOverlay f = getRelatedFolder();
            if (f!=null && f.remove(m)) {
                m.closeInfoWindow();
                mMapView.invalidate();
            } else {
                showErrorMessage(R.string.failure_on_remove_marker);
            }
        }

        public void onOpen(Object arg0) {
            closeAllInfoWindowsOn(this.mMapView);
            if(arg0 instanceof SFSMarker) {
                m = (SFSMarker) arg0;
            }
            TextView txtTitle = (TextView) mView.findViewById(R.id.bubble_title);
            String titleMarker = "";
            try {
                titleMarker = ResourceHelper.getStringResource(R.string.title_marker);

            } catch (InvalidAppConfigException e) {
                Message.showErrorMessage(((MainActivity) mMapView.getContext()), R.string.failure_title_msg, e.getMessage());
            }
            titleMarker += ": " + m.getPosition().toDoubleString();
            txtTitle.setText(titleMarker);

            ImageButton btnMoreInfo = (ImageButton) mView.findViewById(R.id.bubble_moreinfo);
            btnMoreInfo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    long featureID = m.getMarkerId();
                    markerInfoWindowController.viewFeatureData(featureID);
                }
            });
            ImageButton btnEdit = (ImageButton) mView.findViewById(R.id.btn_edit_marker);
            btnEdit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    ImageButton imageButton = (ImageButton)v;
                    imageButton.setVisibility(View.GONE);
                    ProgressBar progressBar = (ProgressBar)mView.findViewById(R.id.pgr_info_window);
                    progressBar.setVisibility(View.VISIBLE);
                    markerInfoWindowController.setProgressBar(progressBar);
                    markerInfoWindowController.setImageBtn(imageButton);

                    markerInfoWindowController.editMarker(m);
                }
            });
            ImageButton btnRemove = (ImageButton) mView.findViewById(R.id.btn_remove_marker);
            btnRemove.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    that.setWhoCall(REMOVE_MAKER);
                    Message.showConfirmMessage((MainActivity) mMapView.getContext(), R.string.title_remove_marker, R.string.message_remove_marker, that);
                }
            });
            ImageButton btnGPS = (ImageButton) mView.findViewById(R.id.btn_move_to_gps);
            btnGPS.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    that.setWhoCall(MOVE_TO_GPS);
                    Message.showConfirmMessage((MainActivity) mMapView.getContext(), R.string.title_move_marker, R.string.message_move_marker_gps, that);

                }
            });
            ImageButton btnMapCenter = (ImageButton) mView.findViewById(R.id.btn_move_to_map_center);
            btnMapCenter.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    that.setWhoCall(MOVE_TO_CENTER);
                    Message.showConfirmMessage((MainActivity) mMapView.getContext(), R.string.title_move_marker, R.string.message_move_marker_center, that);
                }
            });
            ImageButton btnCloseInfo = (ImageButton) mView.findViewById(R.id.btn_close_info_window);
            btnCloseInfo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    m.closeInfoWindow();
                }
            });
        }

        private void showErrorMessage(int msgId) {
            Message.showErrorMessage(
                    (MainActivity)mMapView.getContext(),
                    R.string.title_fail_message_marker,
                    msgId
            );
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
