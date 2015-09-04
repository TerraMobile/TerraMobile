package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DirectedLocationOverlay;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.service.GPSService;
import br.org.funcate.terramobile.util.Message;

/**
 * Created by Andre Carvalho on 02/09/15.
 */
public class GPSOverlayController {

    private DirectedLocationOverlay gpsLayer;
    private final Context context;
    private MapView mapView;
    private LocationListener locationListener;
    private long updateTime;
    private boolean keepOnCenter;

    public GPSOverlayController(Context context) {
        this.context=context;
        if(gpsLayer==null) {
            gpsLayer = new DirectedLocationOverlay(context);
        }
        this.locationListener=null;
        this.updateTime=(1000 * 20);// 20 seconds
        this.keepOnCenter=false;
    }

    /**
     * To set update time used to GPS location provider update location.
     * @param time, in milliseconds (ex.: to 1 minute use (1000 * 60 * 1) or to 20 seconds (1000 * 20) or directly 20000)
     */
    public void setUpdateTime(long time) {
        this.updateTime=time;
    }

    /**
     *
     * @param keepOnCenter
     */
    public void setKeepOnCenter(boolean keepOnCenter) {
        this.keepOnCenter=keepOnCenter;
    }

    /**
     * Verify if "GPS Overlay Layer" is enabled and added on mapView
     * @return, true is active or false otherwise
     */
    public boolean isOverlayAdded() {
        mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        if(mapView==null || gpsLayer==null) return false;
        return (mapView.getOverlays().contains(gpsLayer) && locationListener!=null);
    }

    public void addGPSTrackerLayer(int location) {
        mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        if(mapView==null) return;
        mapView.getOverlays().add(location, gpsLayer);
        enableGPSTrackerLayer();
    }

    public void addGPSTrackerLayer() {
        mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        if(mapView==null) return;
        if(!mapView.getOverlays().contains(gpsLayer)) {
            mapView.getOverlays().add(gpsLayer);
        }
        enableGPSTrackerLayer();
    }

    public void removeGPSTrackerLayer() {
        mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        if(mapView==null) return;
        if(mapView.getOverlays().contains(gpsLayer)) {
            mapView.getOverlays().remove(gpsLayer);
        }
        disableGPSTrackerLayer();
    }

    /**
     * Enable the Layer GPS Overlay and register a listener of the location events.
     */
    public void enableGPSTrackerLayer() {
        mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        if(mapView==null) return;
        if(!gpsLayer.isEnabled()) gpsLayer.setEnabled(true);
        registerOnService();
        GeoPoint lastKnownPoint = new GeoPoint(GPSService.getLastKnownLocation(mapView.getContext()));
        gpsLayer.setLocation(lastKnownPoint);
        mapView.invalidate();
    }

    /**
     * Disable the Layer GPS Overlay and unregister location events.
     */
    public void disableGPSTrackerLayer() {
        mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        if(mapView==null) return;
        if(gpsLayer.isEnabled()) gpsLayer.setEnabled(false);
        unregisterOnService();
        mapView.invalidate();
    }

    private void registerOnService() {

        mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        if(mapView==null) return;
        if(locationListener!=null) {
            unregisterOnService();
        }

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if(mapView==null) return;
                if(mapView.getOverlays().contains(gpsLayer)) {
                    // Called when a new location is found by the GPS location provider.
                    GeoPoint point = new GeoPoint(location);
                    gpsLayer.setLocation(point);
                    if (keepOnCenter)
                        mapView.getController().animateTo(point);
                    else
                        mapView.invalidate();
                }else if(locationListener!=null) {
                    unregisterOnService();
                }
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {
                Message.showErrorMessage((MainActivity) mapView.getContext(), R.string.fail, R.string.disabled_provider);
            }
        };

        if(!GPSService.registerListener(mapView.getContext(), locationListener, updateTime)) {
            Message.showErrorMessage((MainActivity)mapView.getContext(),R.string.fail,R.string.disabled_provider);
        }
    }

    private void unregisterOnService() {
        mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
        if(mapView==null) return;
        GPSService.unregisterListener(mapView.getContext(), locationListener);
    }
}
